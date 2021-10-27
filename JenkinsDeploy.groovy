job('DSL_jobexample1') {
    description('ROBDBuild DSL JOB')
  
    parameters {
        stringParam('BACKUP_DIR', defaultValue = 'ROBD_6.0.8.0', description = 'backup Directory')
        choiceParam('version', ['CM_6.0.8.0', 'CM_6.0.7.0', 'CM_6.0.6.0'])
        choiceParam('WS', ['DSL_jobexample1'])
    }
  
  
  scm {
    git{
      branch('*/master')
      remote {
      	credentials('gitblit')
        url('https://parthasarathi.p@172.16.28.141/r/Campaign_Management/campaign-management.git')
      }
    }
  }
  
  wrappers {
  		withAnt {
          installation('ANT 1.8.2')
        }
  }
  
  steps {
    shell(readFileFromWorkspace('build.sh'))
  }
  
  publishers {
    archiveArtifacts('**/ROBD.war')
    downstreamParameterized {
      trigger('DSL_jobexample2') {
        condition('SUCCESS')
        parameters {
        	currentBuild()
        }
      }
    }
  }
  
  
}


job('DSL_jobexample2') {
    description('ROBDDeployment DSL JOB')
  
    parameters {
      stringParam('BACKUP_DIR', defaultValue = 'ROBD_6.0.8.0', description = 'backup Directory')
      stringParam('WS', defaultValue = '', description = 'UpstreamJobName')
    }
  
    label('231')
  
    wrappers {
        colorizeOutput(colorMap = 'xterm')
    }
  
  steps {
    ansiblePlaybookBuilder {
      playbook('/var/ansible/cmdeploy.yml')
      inventory {
        inventoryPath {
          path('/var/ansible/hosts')
        }
      }
      vaultCredentialsId('vaultpassfile')
      colorizedOutput(colorizedOutput = true)
      extraVars {
        extraVar{
          key('backup_dir')
          value('$BACKUP_DIR')
          hidden(hidden = false)
        }
        extraVar{
          key('build_name')
          value('$WS')
          hidden(hidden = false)
        }
        
      }
      additionalParameters("--extra-vars '@mysql_passkey.yml'")
    }
  }
}
