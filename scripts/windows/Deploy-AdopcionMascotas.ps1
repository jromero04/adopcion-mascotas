[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ArtifactPath,

    [string]$DeployRoot = 'C:\ProgramData\AdopcionMascotas\produccion',

    [ValidateRange(1, 65535)]
    [int]$Port = 8081,

    [string]$JavaHome = $env:JAVA_HOME,

    [ValidateRange(5, 300)]
    [int]$HealthTimeoutSeconds = 45
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$artifact = (Resolve-Path -LiteralPath $ArtifactPath).Path
$deployRootFull = [System.IO.Path]::GetFullPath($DeployRoot).TrimEnd('\')
$driveRoot = [System.IO.Path]::GetPathRoot($deployRootFull).TrimEnd('\')

if ($deployRootFull -eq $driveRoot) {
    throw "El directorio de despliegue no puede ser la raiz de una unidad: $deployRootFull"
}

if ($env:WORKSPACE) {
    $workspaceFull = [System.IO.Path]::GetFullPath($env:WORKSPACE).TrimEnd('\')
    if ($deployRootFull.StartsWith($workspaceFull, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "El despliegue debe quedar fuera del workspace de Jenkins: $workspaceFull"
    }
}

if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    $javaExecutable = (Get-Command java.exe -ErrorAction Stop).Source
} else {
    $javaExecutable = Join-Path $JavaHome 'bin\java.exe'
    if (-not (Test-Path -LiteralPath $javaExecutable -PathType Leaf)) {
        throw "No se encontro Java en $javaExecutable"
    }
}

$appDirectory = Join-Path $deployRootFull 'app'
$backupDirectory = Join-Path $deployRootFull 'backup'
$dataDirectory = Join-Path $deployRootFull 'data'
$logsDirectory = Join-Path $deployRootFull 'logs'
$runDirectory = Join-Path $deployRootFull 'run'

foreach ($directory in @($appDirectory, $backupDirectory, $dataDirectory, $logsDirectory, $runDirectory)) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}

$currentJar = Join-Path $appDirectory 'adopcion-mascotas.jar'
$nextJar = Join-Path $appDirectory 'adopcion-mascotas.next.jar'
$previousJar = Join-Path $backupDirectory 'adopcion-mascotas.jar'
$pidFile = Join-Path $runDirectory 'adopcion-mascotas.pid'
$healthUri = "http://127.0.0.1:$Port/"
$databasePath = (Join-Path $dataDirectory 'adopcion-prod').Replace('\', '/')

function Get-DeployedProcess {
    if (-not (Test-Path -LiteralPath $pidFile -PathType Leaf)) {
        return $null
    }

    $pidText = (Get-Content -LiteralPath $pidFile -Raw).Trim()
    $processId = 0
    if (-not [int]::TryParse($pidText, [ref]$processId)) {
        throw "El archivo PID contiene un valor invalido: $pidFile"
    }

    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $processId" -ErrorAction SilentlyContinue
    if ($null -eq $process) {
        Remove-Item -LiteralPath $pidFile -Force
        return $null
    }

    if ($process.CommandLine.IndexOf($currentJar, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        throw "El PID $processId pertenece a otro proceso; no se detendra automaticamente"
    }

    return $process
}

function Stop-DeployedApplication {
    $process = Get-DeployedProcess
    if ($null -eq $process) {
        return
    }

    Stop-Process -Id $process.ProcessId -Force
    Wait-Process -Id $process.ProcessId -Timeout 15 -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

function Start-DeployedApplication {
    $timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $standardOutput = Join-Path $logsDirectory "aplicacion-$timestamp.log"
    $standardError = Join-Path $logsDirectory "aplicacion-$timestamp.err.log"

    $previousDatabaseUrl = $env:ADOPCION_DB_URL
    $previousServerPort = $env:SERVER_PORT
    $previousJenkinsCookie = $env:JENKINS_NODE_COOKIE

    try {
        $env:ADOPCION_DB_URL = "jdbc:h2:file:$databasePath"
        $env:SERVER_PORT = "$Port"
        $env:JENKINS_NODE_COOKIE = 'adopcion-mascotas-produccion'

        $process = Start-Process `
            -FilePath $javaExecutable `
            -ArgumentList "-jar `"$currentJar`"" `
            -WorkingDirectory $appDirectory `
            -WindowStyle Hidden `
            -RedirectStandardOutput $standardOutput `
            -RedirectStandardError $standardError `
            -PassThru

        Set-Content -LiteralPath $pidFile -Value $process.Id -Encoding ascii
        return $process
    } finally {
        $env:ADOPCION_DB_URL = $previousDatabaseUrl
        $env:SERVER_PORT = $previousServerPort
        $env:JENKINS_NODE_COOKIE = $previousJenkinsCookie
    }
}

function Wait-ForHealthyApplication {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    while ($stopwatch.Elapsed.TotalSeconds -lt $HealthTimeoutSeconds) {
        try {
            $response = Invoke-WebRequest -Uri $healthUri -UseBasicParsing -TimeoutSec 3
            if ($response.StatusCode -eq 200) {
                return $true
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    return $false
}

Copy-Item -LiteralPath $artifact -Destination $nextJar -Force
$hadCurrentVersion = Test-Path -LiteralPath $currentJar -PathType Leaf

try {
    Stop-DeployedApplication

    if ($hadCurrentVersion) {
        Copy-Item -LiteralPath $currentJar -Destination $previousJar -Force
    }

    Move-Item -LiteralPath $nextJar -Destination $currentJar -Force
    $startedProcess = Start-DeployedApplication

    if (-not (Wait-ForHealthyApplication)) {
        throw "La aplicacion nueva no respondio HTTP 200 en $healthUri"
    }

    Write-Output "Despliegue correcto. PID=$($startedProcess.Id) URL=$healthUri"
} catch {
    $deploymentError = $_
    Stop-DeployedApplication

    if ($hadCurrentVersion -and (Test-Path -LiteralPath $previousJar -PathType Leaf)) {
        Copy-Item -LiteralPath $previousJar -Destination $currentJar -Force
        $rollbackProcess = Start-DeployedApplication

        if (-not (Wait-ForHealthyApplication)) {
            throw "Fallo el despliegue y tambien la recuperacion. Error original: $($deploymentError.Exception.Message)"
        }

        throw "Fallo el despliegue; se restauro la version anterior con PID $($rollbackProcess.Id). Error: $($deploymentError.Exception.Message)"
    }

    Remove-Item -LiteralPath $currentJar -Force -ErrorAction SilentlyContinue
    throw "Fallo el primer despliegue y no habia una version anterior. Error: $($deploymentError.Exception.Message)"
} finally {
    Remove-Item -LiteralPath $nextJar -Force -ErrorAction SilentlyContinue
}
