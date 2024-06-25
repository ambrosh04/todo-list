pipeline {
    agent any
    environment {
        AWS_ACCOUNT_ID = '905418111557'
        AWS_REGION = 'us-east-1'
        ECR_REPOSITORY = 'public.ecr.aws/l6s9i6b7/todo-list'
        IMAGE_TAG = 'latest'
        CLUSTER_NAME = 'todo-list'
        TASK_FAMILY = 'todo-list-task'
        SERVICE_NAME = 'todo-list-service'
        CONTAINER_NAME = 'todo-list'
    }
    stages {
        stage('Build') {
            steps {
                echo "Building Docker image..."
                sh 'docker build -t $ECR_REPOSITORY:$IMAGE_TAG .'
            }
        }
        stage('Push to ECR') {
            steps {
                echo "Pushing Docker image to Amazon ECR..."
                script {
                    sh '''
                    aws ecr-public get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin public.ecr.aws
                    docker tag $ECR_REPOSITORY:$IMAGE_TAG $ECR_REPOSITORY:$IMAGE_TAG
                    docker push $ECR_REPOSITORY:$IMAGE_TAG
                    '''
                }
            }
        }
        stage('Register Task Definition') {
            steps {
                echo "Registering ECS Task Definition..."
                script {
                    sh '''
                    aws ecs register-task-definition --family $TASK_FAMILY \
                    --network-mode bridge \
                    --container-definitions '[
                        {
                            "name": "$CONTAINER_NAME",
                            "image": "$ECR_REPOSITORY:$IMAGE_TAG",
                            "cpu": 256,
                            "memory": 512,
                            "portMappings": [
                                {
                                    "containerPort": 8000,
                                    "hostPort": 8000
                                }
                            ],
                            "essential": true
                        }
                    ]'
                    '''
                }
            }
        }
        stage('Deploy to ECS') {
            steps {
                echo "Deploying Docker image to Amazon ECS..."
                script {
                    sh '''
                    aws ecs update-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --force-new-deployment
                    '''
                }
            }
        }
    }
}
