podTemplate(containers: [
    containerTemplate(name: 'maven', image: 'maven:3.8.1-jdk-15', command: 'sleep', args: '99d'),
  ]) {

    node(POD_LABEL) {
        stage('Get a Maven project') {
            container('maven') {
                stage('Build a Maven project') {
                    
                    sh ''' mvn -version
                           mvn clean package
                           ls -a
                           du -h --max-depth=1
                           '''
                }
            }
        }  
    }
}
