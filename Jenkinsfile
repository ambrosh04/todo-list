pipeline {
    agent any
    environment {
        AWS_CREDENTIALS_ID = 'aws-config'
        ECR_REGISTRY = 'public.ecr.aws/z4y3q1f9/todo-list'
        IMAGE_TAG = "latest"
        REPO_URL = 'https://github.com/ambrosh04/todo-list.git'
        ECS_CLUSTER = 'todo-list'
        ECS_SERVICE = 'todo-list-SVC'
        TASK_DEFINITION = 'todo-list-TD'
    }
    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'develop', url: "${REPO_URL}"
            }
        }
        stage('Build and Push Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${ECR_REGISTRY}:${IMAGE_TAG}")
                }
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
                        sh '''
                        aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                        aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY

                        NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
                            --family ${TASK_DEFINITION} \
                            --network-mode bridge \
                            --container-definitions '[
                                {
                                    "name": "todo-container",
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
