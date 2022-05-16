podTemplate(containers: [
    containerTemplate(name: 'maven', image: 'maven:3.8.5-openjdk-11', command: 'sleep', args: '99d'),
  ]) {

    node(POD_LABEL) {
        stage('Get a Maven project') {
            container('maven') {
                stage('Build a Maven project') {
                    checkout scm
                    sh ''' mvn -version
                           pwd
                           mvn clean package
                           ls -a
                           ls -a /home/jenkins/agent/workspace
                           ls -a /home/jenkins/agent/workspace/demo_temp
                           ls target
                           cd target
                           du -h --max-depth=1
                           '''
                }
            }
        }  
    }
}
