pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9.12'
    }

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timestamps()
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verificar entorno') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
            }
        }

        stage('Compilar') {
            steps {
                bat 'mvn -B -ntp clean compile -DskipUnitTests=true'
            }
        }

        stage('Pruebas unitarias') {
            steps {
                bat 'mvn -B -ntp test -DskipITs=true'
            }
        }

        stage('Pruebas de integracion') {
            steps {
                bat 'mvn -B -ntp verify -DskipUnitTests=true'
            }
        }

        stage('Empaquetar') {
            steps {
                bat 'mvn -B -ntp package -DskipUnitTests=true -DskipITs=true'
                archiveArtifacts(
                        artifacts: 'catalogo-web/target/catalogo-web-*.jar',
                        fingerprint: true,
                        onlyIfSuccessful: true
                )
            }
        }
    }

    post {
        always {
            junit(
                    testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml',
                    allowEmptyResults: true
            )
        }
    }
}
