pipeline {
    environment { 

        dockerImage = ''
        dockerContainerMasterPort = '7200'
        dockerContainerDevelopPort = '7300'
        gitUrl = 'https://github.com/superuserz/app_manmeetdevgun.git'
        sonarInstallationName = 'Test_Sonar'
        sonarProjectKey='sonar-manmeetdevgun'
        sonarHost='http://localhost:9000'
        sonarToken='c1084e0ae20a2a86ee4ba7001da3d9d8575411e7'
        dockerCredentialsId='DockerHub'
        username='manmeetdevgun'
        dockerhubUsername='superuserz'
        jacocoXMLCoveragePath='target/site/jacoco/jacoco.xml'
        
        //////////KUBERNETES-CONFIGS/////////
        deploymentfile = 'deployment.yml'
        projectId = 'calcium-rigging-322119'
        clusterName = 'nagp-k8s-jenkins-cluster'
        clusterLocation = 'asia-south1-a'
        serviceAccountKey = 'gke'
        gceAccount = 'manmeet.devgun3152707@gmail.com'
        serviceAccountKeyFile = 'gce.json'
    }
    agent any
    
    tools { 
        maven 'maven3' 
    }
    
    options {

        timeout(time: 1, unit: 'HOURS')
        skipStagesAfterUnstable() 
    }
    
    stages {
        
        stage('Checkout') {
            steps {
                
                script {
                    
                    def checkoutBranch = env.BRANCH_NAME;
                    git poll: true, url: gitUrl, branch: checkoutBranch
                    bat 'dir'
                }
            }
        } //Checkout Stage End.  
        
        stage('Build') {
            steps {
                echo "M2_HOME = ${M2_HOME}"
                echo "${env.BRANCH_NAME}"
                bat 'mvn -B -DskipTests clean package'  //Build the Project
                bat 'mvn verify'        //Generate jcocco reports
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
                            withSonarQubeEnv(sonarInstallationName) {       //Run Sonar Qube Analysis. For Quality gate, you need to setup up a webhook in sonar. Not in scope of this assignment.
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
                     //Build the Docker Image
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
                                dockerImage.push()          //Push the image to Docker Hub
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
                                } catch (Exception err) {
                                    //If no container with same name is running, this step will throw an exceptoin. Handle it and do Nothing.
                                }
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
                            //Run the New image on Docker instance
                        }   
                        if (env.BRANCH_NAME == 'develop') {
                            bat "docker run --pull=allways -itd -p ${dockerContainerDevelopPort}:8080 --name c-${username}-${env.BRANCH_NAME} ${dockerhubUsername}/i-${username}-${env.BRANCH_NAME}:v1"
                            //Run the New image on Docker instance
                            
                        }
                } 
            }
        }//Docker Deploy Stage End
        
        stage('Kubernetes Deployment'){
            steps{
                script{
                    def kubernetesMasterPort = '30157'
                    def kubernetesDevelopPort = '30158'
                    def firewallRuleName = ''
                    if(env.BRANCH_NAME == 'master'){
                        firewallRuleName = 'master-node-port'
                    }
                    if(env.BRANCH_NAME == 'develop'){
                        firewallRuleName = 'develop-node-port'
                    }
                    
                    withCredentials([file(credentialsId: 'gcesakey', variable: 'gcesakey')]) {      //gcesakey refers to the service account key for GCE service account.
                                                                                                    //Ensure NOT to commit the SA key to any public repo.
                        
                    bat "gcloud config set account ${gceAccount}"               //Set gcloud to correct google-account.
                    
                    bat "gcloud auth activate-service-account --key-file $gcesakey"         //Set gcloud to correct google-account's service account.

                    bat "gcloud container clusters get-credentials ${clusterName} --zone ${clusterLocation} --project ${projectId}"  //connect to the gcloud kubernetes cluster.
                    
                    }
            
                    step([$class: 'KubernetesEngineBuilder', projectId: env.projectId, clusterName: env.clusterName, location: env.clusterLocation, manifestPattern: env.deploymentfile, credentialsId: env.serviceAccountKey])
                    try{
                        
                        if(env.BRANCH_NAME == 'master'){
                            bat "gcloud compute firewall-rules create ${firewallRuleName} --allow tcp:${kubernetesMasterPort} --project ${projectId}"   //Set appropraie firewall Rile to connect to VM
                        }
                        
                        if(env.BRANCH_NAME == 'develop'){
                            bat "gcloud compute firewall-rules create ${firewallRuleName} --allow tcp:${kubernetesDevelopPort} --project ${projectId}"  //Set appropraie firewall Rile to connect to VM
                        }
                        
                    }catch(Exception e){
                        //catching exception in case firewall rule already exists
                    }
                }
            }
        } 
    } //stages
}//pipline
