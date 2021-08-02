pipeline {
    environment { 

        dockerImage = ''
        dockerContainerMasterPort = '7200'
        dockerContainerDevelopPort = '7300'
        gitUrl = 'https://github.com/superuserz/app_manmeetdevgun.git'
        sonarInstallationName = 'Test_Sonar'
        sonarProjectKey='sonar-manmeetdevgun'
        sonarHost='http://localhost:9000'
        sonarToken='c1084e0ae20a2a86ee4ba7001da3d9d8575411e7'  //token to bind with sonar project
        dockerCredentialsId='DockerHub'  //Credentials ID to connect to Docker Hub.
        username='manmeetdevgun'  //will be used in image name
        dockerhubUsername='superuserz'  //will be user to push the image to docker
        jacocoXMLCoveragePath='target/site/jacoco/jacoco.xml'  //path to coverage report
        //////////KUBERNETES-CONFIGS/////////
        deploymentfile = 'deployment.yml'
        projectId = 'carbon-airlock-321321'
        clusterName = 'nagp-k8s-jenkins-cluster'
        clusterLocation = 'us-central1-c'
        serviceAccountKey = 'gke'
    }
    agent any
    options {

        timeout(time: 1, unit: 'HOURS')
        skipStagesAfterUnstable() 
    }
    
    //Declare Stages below this
    
    stages {
        
        stage('Build') {
            steps {
                bat 'mvn -B -DskipTests clean package'
                bat 'mvn verify'  //This will crete Jococo Report at target/site/jacoco/jacoco.xml
                bat 'dir target'
            }
        } //Build Stage End.
        
        stage('Unit Testing') {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                bat 'mvn test'
            }
        } //Unit Testing Stage End.
        
        stage('Sonar Analysis') {
            when {
                expression { env.BRANCH_NAME == 'develop' }
            }
            
            steps  {
                            withSonarQubeEnv(sonarInstallationName) {
                            bat "mvn package sonar:sonar \
  				                -Dsonar.host.url=${sonarHost} \
				                -Dsonar.projectKey=${sonarProjectKey} \
				                -Dsonar.login=${sonarToken} \
                                -Dsonar.coverage.jacoco.xmlReportPaths=${jacocoXMLCoveragePath} \
                                -Dsonar.java.binaries=src/main/java"
                            }
            }
        } //Sonar Analysis Stage End.
        
        stage('Docker Image') {
                steps {
                    script {
                     dockerImage = docker.build("${dockerhubUsername}/i-${username}-${env.BRANCH_NAME}:v1")
                    }
                }
        } //Build Docker Stage End
        
        stage('Containers') {
            
            parallel {
                stage('Docker Push') {
                    agent any
                    steps  {
                        script  { 
                                docker.withRegistry( '', dockerCredentialsId ) { 
                                dockerImage.push() 
                                }
                           } 
                    }
                }
                
                stage('Pre-container Check') {
                    agent any
                    steps {
                        script {
                            try {
                                    // stop already running container
                                    bat "docker stop c-${username}-${env.BRANCH_NAME}"
                                    // remove the old container
                                    bat "docker container rm c-${username}-${env.BRANCH_NAME}"
			                        sleep 5 //seconds //give some time for container to stop.
                                } catch (Exception err) {}
                       }
                   }
               }
           }
        }
        stage('Docker Deploy') {
            steps {
                script {
                        
                        if (env.BRANCH_NAME == 'master') {
                            bat "docker run --pull=allways -itd -p ${dockerContainerMasterPort}:8080 --name c-${username}-${env.BRANCH_NAME} ${dockerhubUsername}/i-${username}-${env.BRANCH_NAME}:v1"
                        }
                        if (env.BRANCH_NAME == 'develop') {
                            bat "docker run --pull=allways -itd -p ${dockerContainerDevelopPort}:8080 --name c-${username}-${env.BRANCH_NAME} ${dockerhubUsername}/i-${username}-${env.BRANCH_NAME}:v1"
                        }
                } 
            }
			
		stage('Kubernetes Deployment'){
            steps{
                script{
                    def kubernetesPort = ''
                    def firewallRuleName = ''
                    if(env.BRANCH_NAME == 'master'){
                        kubernetesPort = 30157
                        firewallRuleName = 'master-node-port'
                    }
                    if(env.BRANCH_NAME == 'develop'){
                        kubernetesPort = 30158
                        firewallRuleName = 'develop-node-port'
                    }
                    bat 'dir'
                //    kubernetesPort = kubernetesPort.toInteger()
                    step([$class: 'KubernetesEngineBuilder', projectId: env.projectId, clusterName: env.clusterName, location: env.clusterLocation, manifestPattern: env.deploymentfile, credentialsId: env.serviceAccountKey])
                    try{
                        bat "gcloud compute firewall-rules create ${firewallRuleName} --allow tcp:30157 --project ${projectId}"
                    }catch(Exception e){
                        // nothing to be done here, just catching exception in case firewall rule already exists
                    }
                }
            }
        } 
        }//Docker Deploy Stage End
    } //stages
}//pipline
