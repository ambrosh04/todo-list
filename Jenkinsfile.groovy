pipeline {
    agent any 
    stages {
        stage('Build') { 
            steps {
                echo "This is Build stage."
                sh 'sudo docker build -t todo-list .'
            }
        }
        stage('Test') { 
            steps {
                echo "This is Test stage." 
                sh 'sudo docker run -d -p 8000:8000 todo-list'
            }
        }
        stage('Deploy') { 
            steps {
                echo "Successful" 
            }
        }
    }
}
