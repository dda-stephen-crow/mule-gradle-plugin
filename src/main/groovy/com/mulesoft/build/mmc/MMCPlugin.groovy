/*
 * Copyright 2014 juancavallotti.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mulesoft.build.mmc

import com.mulesoft.build.MulePluginConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This plugin allows easy deployment of the app in MMC through its REST API.
 * Created by juancavallotti on 04/06/14.
 */
class MMCPlugin implements Plugin<Project> {

    private static final Logger logger = LoggerFactory.getLogger(MMCPlugin)

    public static final String FORCE_ENVIRONMENT_PROPERTY = 'mmcEnvironment'

    @Override
    void apply(Project project) {

        logger.debug('Apply the MMC plugin')


        //create the MMC plugin extension.
        def mmcExt = project.extensions.create('mmc', MMCPluginExtension)

        if (project.hasProperty(FORCE_ENVIRONMENT_PROPERTY)) {
            logger.debug("Environment will be forced to: ${project.property(FORCE_ENVIRONMENT_PROPERTY)}")
            project.mmc.forceEnvironment = project.property(FORCE_ENVIRONMENT_PROPERTY)
        }

        //contribute to the DSL as an extension.
        if (project.hasProperty('mule')) {
            project.mule.ext.mmc = mmcExt.&environments
        } else {
            logger.warn('Could not find mule plugin extension, mule plugin might not be applied.')
        }

        //register the plugin
        Task uploadToRepoTask =  project.tasks.create('uploadToRepository', DeployToMMCTask)

        if (project.tasks.findByName('deploy')) {
            project.deploy.dependsOn uploadToRepoTask
        } else {
            logger.error('Project does not contain the \'deploy\' task, you must apply the \'mule\' plugin!!')
        }

        uploadToRepoTask.description = 'Upload the resulting application to the app repository of the Mule Management Console'
        uploadToRepoTask.group = MulePluginConstants.MULE_GROUP

        uploadToRepoTask.dependsOn project.build
    }
}
