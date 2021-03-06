import vars.multiarch

for (arch in multiarch.allArches([
	'armel', // unsupported; upstream considers the arch effectively dead
])) {
	meta = multiarch.meta(getClass(), arch)

	freeStyleJob(meta.name) {
		description(meta.description)
		logRotator { daysToKeep(30) }
		label(meta.label)
		scm {
			git {
				remote {
					url('https://github.com/tianon/docker-brew-ubuntu-core.git')
					name('origin')
					refspec('+refs/heads/master:refs/remotes/origin/master')
				}
				branches('*/master')
			}
		}
		triggers {
			cron('H H * * *')
			scm('H H/6 * * *')
		}
		wrappers { colorizeOutput() }
		steps {
			shell(multiarch.templateArgs(meta, ['dpkgArch']) + '''
# delete deprecated suites
# (piping to "cut" because "git clean" will not delete the directory itself if there's a trailing slash)
git ls-files --others --directory '*/' \\
	| cut -d/ -f1 \\
	| xargs --no-run-if-empty git clean -dfx

echo "$dpkgArch" > arch
echo "$repo" > repo
./update.sh
''' + multiarch.templatePush(meta))
		}
	}
}
