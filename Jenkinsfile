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
        EC2_HOST = '54.90.208.154' // Correct EC2 instance IP
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
                    dockerImage = docker.build("${ECR_REGISTRY}:${IMAGE_TAG}")
                }
            }
        }
        stage('Login to Amazon ECR') {
            steps {
                withCredentials([usernamePassword(credentialsId: AWS_CREDENTIALS_ID, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                    aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                    aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                    aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    '''
                }
            }
        }
        stage('Push Docker Image to ECR') {
            steps {
                script {
                    dockerImage.push("${IMAGE_TAG}")
                }
            }
        }
        stage('Deploy to EC2') {
            steps {
                withCredentials([file(credentialsId: PEM_CREDENTIALS_ID, variable: 'PEM_FILE')]) {
                    script {
                        sh """
                        ssh -i $PEM_FILE -o StrictHostKeyChecking=no ${EC2_USER}@54.90.208.154
                        set -e
                        echo "Pulling Docker image ${ECR_REGISTRY}:${IMAGE_TAG}..."
                        docker pull ${ECR_REGISTRY}:${IMAGE_TAG}
                        echo "Checking for existing container todo-list..."
                        if docker ps -a | grep -q todo-list; then
                            echo "Stopping old container todo-list..."
                            docker stop todo-list || true
                            echo "Removing old container todo-list..."
                            docker rm todo-list || true
                        else
                            echo "No existing container found."
                        fi
                        echo "Starting new container..."
                        docker run -d -p 8000:8000 --name todo-list ${ECR_REGISTRY}:${IMAGE_TAG}
                      
                        """
                    }
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
