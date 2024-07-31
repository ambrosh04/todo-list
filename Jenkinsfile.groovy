pipeline {
    agent any
    environment {
        AWS_ACCOUNT_ID = '905418111557'
        AWS_REGION = 'us-east-1'
        ECR_REPOSITORY = 'public.ecr.aws/l6s9i6b7/todo-list'
        IMAGE_TAG = 'latest'
        CLUSTER_NAME = 'my-cluster'
        SERVICE_NAME = 'my-service'
        CONTAINER_NAME = 'my-container'
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
        stage('Deploy to EKS') {
            steps {
                echo "Deploying Docker image to Amazon EKS..."
                script {
                    withKubeConfig([credentialsId: 'your-kubeconfig-credentials-id']) {
                        sh '''
                        kubectl set image deployment/$SERVICE_NAME $CONTAINER_NAME=$ECR_REPOSITORY:$IMAGE_TAG
                        kubectl rollout status deployment/$SERVICE_NAME
                        '''
                    }
                }
            }
        }
    }
}
