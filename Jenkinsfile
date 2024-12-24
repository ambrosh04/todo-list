pipeline {
    agent any
    environment {
        AWS_CREDENTIALS_ID = 'todo-list-AWS' // Your AWS Credentials ID in Jenkins
        ECR_REGISTRY = 'public.ecr.aws/z4y3q1f9/todo-list' // ECR registry URL
        IMAGE_TAG = "latest"
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
        ECS_CLUSTER = 'todo-list' // ECS Cluster name
        ECS_SERVICE = 'todo-list-SVC' // ECS Service name
        TASK_DEFINITION = 'todo-list-TD' // ECS Task Definition name
        REGION = 'us-east-1' // AWS Region
    }
    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'develop', url: "${REPO_URL}"
            }
        }
        stage('Build and Push Docker Image') {
            steps {
                withAWS(credentials: AWS_CREDENTIALS_ID, region: "${REGION}") {
                    script {
                        // Build Docker image
                        dockerImage = docker.build("${ECR_REGISTRY}:${IMAGE_TAG}")

                        // Authenticate and push Docker image to ECR
                        sh '''
                        aws ecr-public get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        docker push ${ECR_REGISTRY}:${IMAGE_TAG}
                        '''
                    }
                }
            }
        }
        stage('Update ECS Service') {
            steps {
                withAWS(credentials: AWS_CREDENTIALS_ID, region: "${REGION}") {
                    script {
                        // Register a new task definition and update ECS service
                        sh '''
                        echo "Registering new task definition..."
                        NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
                            --family ${TASK_DEFINITION} \
                            --network-mode bridge \
                            --container-definitions '[
                                {
                                    "name": "todo-list",
                                    "image": "${ECR_REGISTRY}:${IMAGE_TAG}",
                                    "memory": 512,
                                    "cpu": 256,
                                    "essential": true,
                                    "portMappings": [
                                        {
                                            "containerPort": 8000,
                                            "hostPort": 8000,
                                            "protocol": "tcp"
                                        }
                                    ]
                                }
                            ]' \
                            --requires-compatibilities "EC2" \
                            --query 'taskDefinition.taskDefinitionArn' --output text)

                        echo "Updating ECS service..."
                        aws ecs update-service \
                            --cluster ${ECS_CLUSTER} \
                            --service ${ECS_SERVICE} \
                            --task-definition $NEW_TASK_DEF_ARN \
                            --desired-count 1
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
