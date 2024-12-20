pipeline {
    agent any
    tools {
        git 'DefaultGit' // Specify the Git tool configured in Jenkins Global Tool Configuration
    }
    environment {
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
        PEM_CREDENTIALS_ID = 'secret-key' // ID of the secret text holding the PEM file
        EC2_USER = 'ubuntu'
        EC2_HOST = '54.90.208.154' // Deployment server IP
        IMAGE_TAG = "latest"
        APP_NAME = "todo-list"
    }
    stages {
        stage('Login to Deployment Server') {
            steps {
                withCredentials([file(credentialsId: PEM_CREDENTIALS_ID, variable: 'PEM_FILE')]) {
                    sh """
                    ssh -i $PEM_FILE -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'echo "Connected to deployment server"'
                    """
                }
            }
        }
        stage('Clone Repository') {
            steps {
                git branch: 'develop', url: "${REPO_URL}"
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${APP_NAME}:${IMAGE_TAG}")
                }
            }
        }
        stage('Deploy Application') {
            steps {
                withCredentials([file(credentialsId: PEM_CREDENTIALS_ID, variable: 'PEM_FILE')]) {
                    sh """
                    ssh -i $PEM_FILE -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} "
                    set -e
                    echo 'Pulling Docker image...'
                    docker save ${APP_NAME}:${IMAGE_TAG} | docker load
                    echo 'Checking for existing container ${APP_NAME}...'
                    if docker ps -a | grep -q ${APP_NAME}; then
                        echo 'Stopping old container...'
                        docker stop ${APP_NAME} || true
                        echo 'Removing old container...'
                        docker rm ${APP_NAME} || true
                    fi
                    echo 'Starting new container...'
                    docker run -d -p 8000:8000 --name ${APP_NAME} ${APP_NAME}:${IMAGE_TAG}
                    "
                    """
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
