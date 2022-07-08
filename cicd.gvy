pipeline {
    agent any
    environment {
        //be sure to replace "bhavukm" with your own Docker Hub username
        DOCKER_IMAGE_NAME = "pksharathkumar/train-schedule"
    }
    stages {
        stage('Build') {
            steps {
                echo 'Running build automation'
                sh './gradlew build --no-daemon'
                archiveArtifacts artifacts: 'dist/trainSchedule.zip'
            }
        }
        stage('build & push docker image') {
	         steps {
              withDockerRegistry(credentialsId: 'DOCKER_HUB_LOGIN', url: 'https://index.docker.io/v1/') {
                    sh script: 'cd  $WORKSPACE'
                    sh script: 'docker build --file Dockerfile --tag docker.io/pksharathkumar/train-schedule:$BUILD_NUMBER .'
                    sh script: 'docker push docker.io/pksharathkumar/train-schedule:$BUILD_NUMBER'
              }
            }  	
          }		
        }
    stage('DeployToProduction') {
  	  steps {
              sh 'ansible-playbook --inventory /tmp/inv $WORKSPACE/deploy-kube.yml --extra-vars "env=qa build=$BUILD_NUMBER"'
	   }
	   post { 
              always { 
                cleanWs() 
	      }
	   }
	}
}
