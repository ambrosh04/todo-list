pipeline {
    agent any
    tools {
        git 'DefaultGit' // Specify the Git tool configured in Jenkins Global Tool Configuration
    }
    environment {
        AWS_CREDENTIALS_ID = 'aws-config' // Set your AWS credentials ID from Jenkins
        ECR_REGISTRY = 'public.ecr.aws/z4y3q1f9/todo-list' // Your ECR registry URL
        IMAGE_TAG = "latest"
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
        PEM_CREDENTIALS_ID = 'secret-key' // ID of the secret text holding the PEM file
        EC2_USER = 'ubuntu'
        EC2_HOST = '54.90.208.154' // Deployment server IP
        APP_NAME = "todo-list"
    }
    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'develop', url: "${REPO_URL}"
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // Build a new Docker image
                    dockerImage = docker.build("${ECR_REGISTRY}:${IMAGE_TAG}")
                    
                    // Remove any existing local image
                    sh """
                    if docker images -q ${ECR_REGISTRY}:${IMAGE_TAG}; then
                        echo "Removing previous image..."
                        docker rmi -f ${ECR_REGISTRY}:${IMAGE_TAG} || true
                    fi
                    """
                }
            }
        }
        stage('Push Docker Image to ECR') {
            steps {
                withCredentials([usernamePassword(credentialsId: AWS_CREDENTIALS_ID, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                    aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                    aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                    aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    docker push ${ECR_REGISTRY}:${IMAGE_TAG}
                    '''
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
                    docker pull ${ECR_REGISTRY}:${IMAGE_TAG}
                    echo 'Checking for existing container ${APP_NAME}...'
                    if docker ps -a | grep -q ${APP_NAME}; then
                        echo 'Stopping and removing old container...'
                        docker stop ${APP_NAME} || true
                        docker rm ${APP_NAME} || true
                    fi
                    echo 'Checking for existing image...'
                    if docker images -q ${ECR_REGISTRY}:${IMAGE_TAG}; then
                        echo 'Removing old image...'
                        docker rmi -f ${ECR_REGISTRY}:${IMAGE_TAG} || true
                    fi
                    echo 'Starting new container...'
                    docker run -d -p 8000:8000 --name ${APP_NAME} ${ECR_REGISTRY}:${IMAGE_TAG}
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
