// Pipeline da branch main. O job é criado pelo ci/jenkins/casc.yaml.
// Os testes de API entram numa etapa própria, no cartão dos testes de API.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Build e testes') {
            steps {
                sh 'chmod +x mvnw && ./mvnw -B verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
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
    }
}
