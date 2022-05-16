podTemplate(containers: [
    containerTemplate(name: 'maven', image: 'maven:3.8.5-openjdk-11', command: 'sleep', args: '99d'),
  ]) {

    node(POD_LABEL) {
        stage('Get a Maven project') {
            container('maven') {
                stage('Build a Maven project') {
                    git 'https://github.com/DuongVu101498/netty-server.git'
                    sh ''' mvn -version
                           ls -a ..
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
