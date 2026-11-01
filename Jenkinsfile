// Pipeline da branch main. O job é criado pelo ci/jenkins/casc.yaml.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        // Testes unitários e de integração; o JaCoCo falha o build abaixo de 80% de cobertura de linhas.
        stage('Build e testes') {
            steps {
                sh 'chmod +x mvnw && ./mvnw -B verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        // Sobe o jar que acabou de ser compilado, no banco torque_api_test e na porta 8081, e roda o apitest contra ele.
        stage('Testes de API') {
            environment {
                SERVER_PORT = '8081'
                DB_URL = 'jdbc:postgresql://postgres:5432/torque_api_test'
                MAIL_HOST = 'smtp'
                PORTAL_URL = 'http://localhost:8081/portal'
            }
            steps {
                sh '''
                    java -jar target/torque-*.jar > app-api.log 2>&1 &
                    APP=$!
                    trap 'kill $APP' EXIT

                    for i in $(seq 1 60); do
                        curl -sf http://localhost:8081/v3/api-docs > /dev/null && break
                        sleep 2
                    done
                    curl -sf http://localhost:8081/v3/api-docs > /dev/null || { tail -50 app-api.log; exit 1; }

                    # O apitest lê as mesmas variáveis da aplicação: SERVER_PORT, DB_*, TORQUE_ADMIN_*
                    ./mvnw -B -f apitest/pom.xml test
                '''
            }
            post {
                always {
                    junit testResults: 'apitest/target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'app-api.log', allowEmptyArchive: true
                }
            }
        }

        stage('Análise no SonarQube') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh './mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar -Dsonar.projectKey=torque -Dsonar.projectName=Torque'
                }
            }
        }

        // Espera o veredito do Quality Gate, que o SonarQube manda pelo webhook /sonarqube-webhook/ do Jenkins.
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
