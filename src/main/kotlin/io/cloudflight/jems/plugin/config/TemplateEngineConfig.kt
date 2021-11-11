package io.cloudflight.jems.plugin.config

import io.cloudflight.jems.plugin.standard.common.template.CLF_DIALECT_PREFIX
import io.cloudflight.jems.plugin.standard.common.template.ClfDialect
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver

const val PLUGIN_DEFAULT_TEMPLATE_ENGINE = "pluginDefaultTemplateEngine"

@Configuration
class TemplateConfig {

    @Bean
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    fun pluginDefaultTemplateEngine(): ITemplateEngine =
        SpringTemplateEngine().also {
            it.addDialect(Java8TimeDialect())
            it.addTemplateResolver(htmlTemplateResolver())
            it.addDialect(CLF_DIALECT_PREFIX, ClfDialect())
        }

    private fun htmlTemplateResolver(): ITemplateResolver =
        ClassLoaderTemplateResolver().also {
            it.prefix = "/templates/"
            it.suffix = ".html"
            it.templateMode = TemplateMode.HTML
            it.characterEncoding = "UTF-8"
            it.isCacheable = true
            it.checkExistence = true
        }

}
