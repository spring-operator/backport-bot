def projectProperties = [
	[$class: 'BuildDiscarderProperty',
		strategy: [$class: 'LogRotator', numToKeepStr: '5']],
	pipelineTriggers([cron('@daily')])
]
properties(projectProperties)

def SUCCESS = hudson.model.Result.SUCCESS.toString()
currentBuild.result = SUCCESS

try {
	parallel check: {
		stage('Check') {
			node {
				checkout scm
				try {
					sh "./gradlew clean assemble check --no-daemon --stacktrace"
				} catch(Exception e) {
					currentBuild.result = 'FAILED: check'
					throw e
				} finally {
					junit '**/build/test-results/*/*.xml'
				}
			}
		}
	}

	if(currentBuild.result == 'SUCCESS') {
		parallel deploy: {
			stage('Deploy Application') {
				node {
					checkout scm
					sh "./ci/scripts/install-cf.sh"
					withCredentials([usernamePassword(credentialsId: 'backportbot-cf', passwordVariable: 'CF_PASSWORD', usernameVariable: 'CF_USERNAME')]) {
						sh "./cf login -a api.run.pivotal.io -o FrameworksAndRuntimes -s rwinch -u '$CF_USERNAME' -p '$CF_PASSWORD'"
					}
					withCredentials([string(credentialsId: 'backportbot-issuemaster-personal-access-token', variable: 'ISSUEMASTER_PERSONAL_ACCESS_TOKEN')]) {
						withCredentials([usernamePassword(credentialsId: 'backportbot-client-registration', passwordVariable: 'CLIENT_SECRET', usernameVariable: 'CLIENT_ID')]) {
							withCredentials([string(credentialsId: 'backportbot-github-webhook-secret', variable: 'GITHUB_WEBHOOK_SECRET')]) {
								sh "./ci/scripts/cf-push.sh backportbot \"--var CLIENT_ID=$CLIENT_ID --var CLIENT_SECRET=$CLIENT_SECRET --var GITHUB_WEBHOOK_SECRET=$GITHUB_WEBHOOK_SECRET --var ISSUEMASTER_PERSONAL_ACCESS_TOKEN=$ISSUEMASTER_PERSONAL_ACCESS_TOKEN --var INFO_VERSION=build-${env.BUILD_NUMBER}\""
							}
						}
					}
				}
			}
		}
	}
} catch(Exception e) {
	currentBuild.result = 'FAILED: deploys'
	throw e
} finally {
	def buildStatus = currentBuild.result
	def buildNotSuccess =  !SUCCESS.equals(buildStatus)
	def lastBuildNotSuccess = !SUCCESS.equals(currentBuild.previousBuild?.result)

	if(buildNotSuccess || lastBuildNotSuccess) {

		stage('Notifiy') {
			node {
				final def RECIPIENTS = [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]

				def subject = "${buildStatus}: Build ${env.JOB_NAME} ${env.BUILD_NUMBER} status is now ${buildStatus}"
				def details = """The build status changed to ${buildStatus}. For details see ${env.BUILD_URL}"""

				emailext (
					subject: subject,
					body: details,
					recipientProviders: RECIPIENTS,
					to: "rwinch@pivotal.io"
				)
			}
		}
	}
}
