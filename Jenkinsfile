pipeline {
    agent any
    tools {
        git 'Default' // Specify the Git tool configured in Jenkins Global Tool Configuration
    }
    environment {
        AWS_CREDENTIALS_ID = 'todo-list-AWS' // Set your AWS credentials ID from Jenkins
        ECR_REGISTRY = 'public.ecr.aws/z4y3q1f9/todo-list' // Your ECR registry URL
        IMAGE_TAG = "latest"
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
        ECS_CLUSTER = 'todo-list' // ECS Cluster name
        ECS_SERVICE = 'todo-list-SVC' // ECS Service name
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
        stage('Update ECS Service') {
            steps {
                withCredentials([usernamePassword(credentialsId: AWS_CREDENTIALS_ID, usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    script {
                        // Update the ECS service with the latest Docker image
                        sh '''
                        aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                        aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY

                        echo "Updating ECS service with latest image..."
                        aws ecs update-service \
                            --cluster ${ECS_CLUSTER} \
                            --service ${ECS_SERVICE} \
                            --force-new-deployment
                        '''
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
