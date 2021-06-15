pipeline {
    agent none
    environment {
                DEPLOY_TO_PRODUCTION = 'true'
            }
    stages{
        stage('run on linux'){
            agent {label 'linux'}
            stages {
              stage('Build') {
                agent {
                    docker {
                       image 'maven:3.8.1-adoptopenjdk-15'
                       args '-v $HOME/.m2:/root/.m2'
                       reuseNode true
                    }
                 }
                steps {
                     sh '''mvn clean package
                           '''
                      }
              }
             stage("Build Docker image") {
                steps {
                  script {
                    docker_image = docker.build("duongvt16/netty-app:${env.BUILD_ID}")
                  }
                }
             }

             stage("Push images") {
                steps {
                    script {
                      withDockerRegistry(credentialsId: 'docker-hub-secret') {
                          docker_image.push("test-${env.BUILD_ID}")
                      }
                    }
                }
              }
              stage('Staging deploy'){
                  steps {
                      sh ''' cat "k8s/staging-deploy.yaml" | sed "s/{{BUILD_ID}}/$BUILD_ID/g" | kubectl apply -n staging -f -
                             kubectl rollout status deployment.apps/netty -n staging --timeout=2m
                          '''
                  }
              }
              stage('Load testing'){
                 agent {label 'window'}
                  steps {
                          build job: 'load-test-2'
                        }
                  post{
                      always{
                          node('linux'){
                              sh ''' kubectl delete service/netty deployment.apps/netty -n staging'''
                          }
                      }
                  }
              }
              stage('Production deploy'){
                  when{
                      beforeOptions true
                      environment name: 'DEPLOY_TO_PRODUCTION', value: 'true'
                  }
                  steps {
                    script {
                      withDockerRegistry(credentialsId: 'docker-hub-secret') {
                          docker_image.push("production-${env.BUILD_ID}")
                          docker_image.push('latest')
                      }
                    }
                    sh ''' cat "k8s/production-deploy.yaml" | sed "s/{{BUILD_ID}}/$BUILD_ID/g" | kubectl apply -n production --record=true -f -
                         kubectl rollout status deployment.apps/netty -n production --timeout=2m
                     '''
                  }
              }
                
           }
        }
    }
     post {
        always {
            node('linux'){
               echo 'One way or another, I have finished'
            }
        }
        success {
            node('linux'){
               echo 'I succeeded!'
               sh  '''   kubectl describe deployment netty -n production
                         kubectl rollout history deployment.apps/netty -n production
                         kubectl get rs -n production
               '''
            }
        }
        unstable {
            node('linux'){
               echo 'I am unstable :/'
            }
        }
        failure {
            node('linux'){
               echo 'I failed :('
            }
        }
    }
}

