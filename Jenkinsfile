pipeline {
    agent any
    environment {
        AWS_CREDENTIALS_ID = 'aws-config'
        ECR_REGISTRY = 'public.ecr.aws/z4y3q1f9/todo-list'
        ECR_REPO = 'todo-list'
        IMAGE_TAG = "latest"
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
        PEM_CREDENTIALS_ID = 'secret-key'
        EC2_USER = 'ubuntu'
        EC2_HOST = '54.160.167.184'
    }
    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'develop', url: "${REPO_URL}"
            }
        }
        stage('Build and Tag Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${ECR_REGISTRY}:${env.BUILD_NUMBER}")
                }
            }
        }
        stage('Login to Amazon ECR and Push Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: AWS_CREDENTIALS_ID, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                    aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                    aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                    aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    '''
                    sh "docker push ${ECR_REGISTRY}:${env.BUILD_NUMBER}"
                }
            }
        }
        stage('Deploy to EC2') {
            steps {
                withCredentials([file(credentialsId: PEM_CREDENTIALS_ID, variable: 'PEM_FILE')]) {
                    script {
                        sh """
                        ssh -i $PEM_FILE -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} << EOF
                        docker pull ${ECR_REGISTRY}:${env.BUILD_NUMBER}
                        docker stop todo-list || true
                        docker rm todo-list || true
                        docker run -d -p 8000:8000 --name todo-list ${ECR_REGISTRY}:${env.BUILD_NUMBER}
                        EOF
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
