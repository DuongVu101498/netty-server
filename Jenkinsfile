podTemplate(containers: [
    containerTemplate(name: 'maven', image: 'maven:3.8.5-openjdk-11', command: 'sleep', args: '99d'),
  ]) {

    node(POD_LABEL) {
        stage('Get a Maven project') {
            container('maven') {
                stage('Build a Maven project') {
                    sh ''' mvn -version
                           pwd
                           ls -a
                           du -h --max-depth=1
                           mvn clean package
                           ls target
                           cd target
                           du -h --max-depth=1
                           '''
                }
            }
        }  
    }
}
