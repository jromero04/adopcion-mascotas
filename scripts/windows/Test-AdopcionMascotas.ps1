[CmdletBinding()]
param(
    [uri]$Uri = 'http://127.0.0.1:8081/',

    [string]$ExpectedContent = 'Registrar mascota',

    [ValidateRange(1, 60)]
    [int]$TimeoutSeconds = 10
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$response = Invoke-WebRequest `
    -Uri $Uri `
    -UseBasicParsing `
    -TimeoutSec $TimeoutSeconds

if ($response.StatusCode -ne 200) {
    throw "La aplicacion respondio HTTP $($response.StatusCode) en $Uri"
}

if ($response.Content.IndexOf($ExpectedContent, [System.StringComparison]::Ordinal) -lt 0) {
    throw "La respuesta de $Uri no contiene el texto esperado: $ExpectedContent"
}

Write-Output "Comprobacion correcta. HTTP 200 y contenido esperado en $Uri"
