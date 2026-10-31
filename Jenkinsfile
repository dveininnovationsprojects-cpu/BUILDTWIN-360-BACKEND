// BuildTwin 360 Backend - CI pipeline (Spring Boot 3.4 / Java 21 / Maven wrapper)
//
// Jenkins prerequisites:
//   * Tool "JDK21" configured in Manage Jenkins > Tools (Temurin/OpenJDK 21)
//   * Plugins: Pipeline, JUnit, Git
//
// Tests need no external services: controller tests are @WebMvcTest slices and the
// context test uses the 'test' profile (H2 in-memory, see src/test/resources).

pipeline {
    agent any

    tools {
        jdk 'JDK21'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds(abortPrevious: true)
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository -Xmx1g'
        MVN        = './mvnw -B -ntp'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'   // mvnw is committed without the executable bit
                    }
                }
            }
        }

        stage('Environment Info') {
            steps {
                script {
                    run('java -version')
                    run("${env.MVN} -v")
                }
            }
        }

        stage('Compile') {
            steps {
                script { run("${env.MVN} clean compile") }
            }
        }

        stage('Test') {
            steps {
                script { run("${env.MVN} test") }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                // Tests already ran in the previous stage
                script { run("${env.MVN} package -DskipTests") }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
                }
            }
        }

    }

    post {
        success { echo 'CI passed.' }
        failure { echo 'CI failed - check the failing stage and the JUnit report.' }
        cleanup { cleanWs(deleteDirs: true, notFailBuild: true) }
    }
}

// Runs a command with the right shell for the agent OS (Linux or Windows).
def run(String cmd) {
    if (isUnix()) {
        sh cmd
    } else {
        bat cmd.replace('./mvnw', 'mvnw.cmd')
    }
}
