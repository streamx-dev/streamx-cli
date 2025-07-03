# **StreamX Dev Repository Template**

This repository serves as a **template** for creating new repositories within the **streamx-dev** organization. It includes pre-configured settings, templates, and best practices to ensure consistency and compliance across all projects.

## **Included Configurations**

1. **EULA License:**
   - A sample **End-User License Agreement (EULA)** is included for repositories that require it.
   - Located in the `LICENSE.md` file.
  
2. **Issue Templates:**
   - Standardized templates for reporting bugs, requesting features, and general contacts links.
   - Located in the `.github/ISSUE_TEMPLATE/` directory.

3. **Pull Request (PR) Templates:**
   - A default PR template is provided to maintain quality and structure in submissions.
   - Located in the `.github/PULL_REQUEST_TEMPLATE.md` file.

4. **Codeowners:**
   - Defines the responsible individuals or teams for reviewing changes.
   - Located in the `.github/CODEOWNERS` file.
  
5. **Dependabot:**
   - It is a tool that automates dependency updates, ensuring your project stays up-to-date with the latest security patches and version improvements.
   - Located in the `.github/dependabot.yml` file.
   - The provided Dependabot setup performs a monthly scan of the subfolders in your repository for Maven projects, checking dependencies for outdated versions. If any outdated dependencies are detected, Dependabot automatically creates a new Pull Request (PR) with the updates.
   - It is often necessary to establish a CI/CD pipeline to validate and test updates whenever a new PR is opened. These pipelines require specific credentials to operate effectively. However, it is important to note that pipelines triggered by Dependabot use a different set of variables and secrets than those triggered by GitHub users manually opening a PR.
Please contact the Principal Engineer responisble for Infrastructure to provide the appropriate secrets for Dependabot-triggered pipelines.
   - You can learn more about managing secrets for Dependabot-triggered pipelines in the [GitHub documentation on Accessing Secrets in Dependabot](https://docs.github.com/en/code-security/dependabot/troubleshooting-dependabot/troubleshooting-dependabot-on-github-actions#accessing-secrets).

## **Repository Settings Standards** 

1. **Main Branch Protection Rules**
   - The main branch is protected to maintain code quality and stability.
   - Merging to `main` requires: a review with approval from at least one **code owner**.
  
3. **Default reposiotry settings**:
   - Wikis - disabled
   - Issues - enabled
   - for PRs only squash merging are allowed
   - Automatically delete head branches - allowed

## **How to Use This Template**
1. **Clone or Use as Template:**  
   - Create by cloning the repository.
   - Create a new repository based on this template. See example below:
<img width="758" alt="image" src="https://github.com/user-attachments/assets/91bc7776-e7ef-421e-9405-abf68f1e5013" />

2. **Update Configurations:**
   - **Adjust the License:** If needed, replace the default license with one that matches your project’s requirements. When there are any dobts, check the [decision log](https://teamds.atlassian.net/wiki/x/AYA1KQ) related to Licensing Policy.
   - **Modify codeowners:** Ensure the responsible teams or individuals are correctly listed in `.github/CODEOWNERS`. It's importnant to avoid merge bottlenecks.
   - **Adjust Dependabot configuration:** Adjust dependabot config to fit the project needs. Provide CI/CD pipelines to validate and test updates, as well. 
   - **Provide a JIRA release GitHub action:** [here](https://github.com/streamx-dev/streamx-common-github-actions) you can find more details on how and why to create it.
   - **Update README.md:** Add specific details about your project to replace this template content.

3. **Start Developing:**  
   - Get familiar with [Contribution Policy](https://github.com/streamx-dev/streamx/blob/main/CONTRIBUTING.md).
   - Push your code, create issues, and submit PRs following the provided templates.

## **Best Practices**
- Regularly review the codeowners file to ensure it reflects the correct reviewers.
- Keep your license file up to date with the project’s purpose and legal requirements.
- Ensure all team members are familiar with the repository’s templates and protection rules.

By using this template, you’re setting up your repository for success with clear structure, strong protections, and organizational consistency.  
