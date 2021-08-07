
pipeline {
    environment { 
	
		USERNAME='manmeetdevgun'

		/////////GIT-CONFIGS///////////
		GITURL = 'https://github.com/superuserz/app_manmeetdevgun.git'
		
		
		////////SONAR-CONFIGS/////////
		SONAR_INSTALLATION = 'Test_Sonar'
        SONAR_PROJECTKEY = 'sonar-manmeetdevgun'
        SONAR_HOST = 'http://localhost:9000'
        SONAR_TOKEN = 'c1084e0ae20a2a86ee4ba7001da3d9d8575411e7'
		SONAR_COVERAGEPATH = 'target/site/jacoco/jacoco.xml'
		
		////////DOCKER-CONFIGS////////
        DOCKER_CONTAINER_MASTER_PORT = '7200'
        DOCKER_CONTAINER_DEVELOP_PORT = '7300'
		DOCKER_REPO = 'superuserz'
		

        
        //////////KUBERNETES-CONFIGS/////////
		KUBERNETES_DEPLOYMENTFILE = 'deployment.yml'
		GCE_PROJECTID = 'calcium-rigging-322119'
		GCE_CLUSTER = 'nagp-k8s-jenkins-cluster'
		GCE_CLUSTERLOCATION = 'asia-south1-a'
		GCE_JENKINS_SA_KEY = 'gke'
		GCE_ACCOUNT = 'manmeet.devgun3152707@gmail.com'

    }
    agent any
    
    tools { 
        maven 'maven3' 
    }
    
    options {

        timeout(time: 1, unit: 'HOURS')
        skipStagesAfterUnstable() 
    }
    
    parameters {
        choice(name: 'Branch', choices: ['master', 'develop'], description: 'Select Branch')
    }
    //Decalare Stages below this
    
    stages {
        
        stage('Checkout') {
            steps {
                
                script {
                    
                    def checkoutBranch = params.Branch;
                    git poll: true, url: GITURL, branch: checkoutBranch
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
                expression { params.Branch == 'master' }
            }
            steps {
                bat 'mvn test'
            }
        } //Unit Testing Stage End.
        
        stage('Sonar Analysis') {
            when {
                expression { params.Branch == 'develop' }
            }
            
            steps  {
                            withSonarQubeEnv(SONAR_INSTALLATION) {       //Run Sonar Qube Analysis. For Quality gate, you need to setup up a webhook in sonar. Not in scope of this assignment.
                            bat "mvn package sonar:sonar \
  				                -Dsonar.host.url=${SONAR_HOST} \
				                -Dsonar.projectKey=${SONAR_PROJECTKEY} \
				                -Dsonar.login=${SONAR_TOKEN} \
                                -Dsonar.coverage.jacoco.xmlReportPaths=${SONAR_COVERAGEPATH} \
                                -Dsonar.java.binaries=src/main/java"
                            }
            }
        } //Sonar Analysis Stage End.
        
        stage('Build Docker Image') {
                steps {
                    script {
                        bat "docker build -t ${DOCKER_REPO}/i-${USERNAME}-${params.Branch}:v1 ."
                    }
                }
        } //Build Docker Stage End
        
        stage('Containers') {
            
            parallel {
                stage('Docker Push') {
                    agent any
                    steps  {
                        script  { 
                                    withCredentials([usernamePassword(credentialsId: 'DockerHub', passwordVariable: 'dockerpassword', usernameVariable: 'dockerusername')]) {
                                        bat "docker login -u ${dockerusername} -p ${dockerpassword}"
                                        
                                        bat "docker push ${DOCKER_REPO}/i-${USERNAME}-${params.Branch}:v1"
                                    }
                                }
                           } 
                }
                
                stage('Pre-container Check') {
                    agent any
                    steps {
                        script {
                            try {
                                    bat "docker stop c-${USERNAME}-${params.Branch}"   // stop already running container
                                    
                                    bat "docker container rm c-${USERNAME}-${params.Branch}"  // remove the old container
                                    
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
                        
                        if (params.Branch == 'master') {
                            bat "docker run --pull=allways -itd -p ${DOCKER_CONTAINER_MASTER_PORT}:8080 --name c-${USERNAME}-${params.Branch} ${DOCKER_REPO}/i-${USERNAME}-${params.Branch}:v1"
                            //Run the New image on Docker instance
                        }   
                        if (params.Branch == 'develop') {
                            bat "docker run --pull=allways -itd -p ${DOCKER_CONTAINER_DEVELOP_PORT}:8080 --name c-${USERNAME}-${params.Branch} ${DOCKER_REPO}/i-${USERNAME}-${params.Branch}:v1"
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
                    if(params.Branch == 'master'){
                        firewallRuleName = 'master-node-port'
                    }
                    if(params.Branch == 'develop'){
                        firewallRuleName = 'develop-node-port'
                    }
                    
                    withCredentials([file(credentialsId: 'gcesakey', variable: 'gcesakey')]) {      //gcesakey refers to the service account key for GCE service account.
                                                                                                    //Ensure NOT to commit the SA key to any public repo.
                        
                    bat "gcloud config set account ${GCE_ACCOUNT}"               //Set gcloud to correct google-account.
                    
                    bat "gcloud auth activate-service-account --key-file $gcesakey"         //Set gcloud to correct google-account's service account.

                    bat "gcloud container clusters get-credentials ${GCE_CLUSTER} --zone ${GCE_CLUSTERLOCATION} --project ${GCE_PROJECTID}"  //connect to the gcloud kubernetes cluster.
                    
                    bat "kubectl apply -f ${KUBERNETES_DEPLOYMENTFILE}"
                    
                    bat "kubectl set image deployment i-${USERNAME}-${params.Branch} i-${USERNAME}-${params.Branch}=${DOCKER_REPO}/i-${USERNAME}-${params.Branch}:v1"
                    
                    }
            
                /*    step([$class: 'KubernetesEngineBuilder', projectId: env.GCE_PROJECTID, clusterName: env.GCE_CLUSTER, location: env.GCE_CLUSTERLOCATION, manifestPattern: env.KUBERNETES_DEPLOYMENTFILE, credentialsId: env.GCE_JENKINS_SA_KEY])
                    try{
                        
                        if(params.Branch == 'master'){
                            bat "gcloud compute firewall-rules create ${firewallRuleName} --allow tcp:${kubernetesMasterPort} --project ${GCE_PROJECTID}"   //Set appropraie firewall Rile to connect to VM
                        }
                        
                        if(params.Branch == 'develop'){
                            bat "gcloud compute firewall-rules create ${firewallRuleName} --allow tcp:${kubernetesDevelopPort} --project ${GCE_PROJECTID}"  //Set appropraie firewall Rile to connect to VM
                        }
                        
                        
                        
                    }catch(Exception e){
                        //catching exception in case firewall rule already exists
                    }*/
                }
            }
        } 
    } //stages
}//pipline
