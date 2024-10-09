pipeline {
    agent any
    environment {
        AWS_CREDENTIALS_ID = 'aws-config' // Set your AWS credentials ID from Jenkins
        ECR_REGISTRY = 'public.ecr.aws/z4y3q1f9/todo-list' // Your ECR registry URL
        ECR_REPO = 'todo-list' // Your ECR repository name
        IMAGE_TAG = "latest"
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
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
                    dockerImage = docker.build("${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}")
                }
            }
        }
        stage('Login to Amazon ECR') {
            steps {
                script {
                    sh '''
                    aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/z4y3q1f9
                }
            }
        }
        stage('Push Docker Image to ECR') {
            steps {
                script {
                    dockerImage.push()
                }
            }
        }
        stage('Update Deployment YAML') {
            steps {
                script {
                    sh '''
                    sed -i 's|image: .*|image: public.ecr.aws/z4y3q1f9/todo-list:latest|' k8s/deployment.yaml
                    git add k8s/deployment.yaml
                    git commit -m "Updated deployment image"
                    git push origin main
                    '''
                }
            }
        }
    }
}
