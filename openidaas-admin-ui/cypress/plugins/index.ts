/// <reference types="cypress" />

/**
 * This function is called when a project is opened or re-opened (e.g. when running `cypress open`).
 */
export default function (on: Cypress.PluginEvents, config: Cypress.PluginConfigOptions) {
  // on('file:preprocessor', filePreprocessor)

  // Node events
  on('task', {
    log(message: string) {
      console.log(message)
      return null
    }
  })

  return config
}
