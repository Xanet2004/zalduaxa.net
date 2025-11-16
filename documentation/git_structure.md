# Git Branch Structure
[Back to menu](/README.md)

- **main**: Production-ready state. Only stable and tested code should be merged here.  
- **develop**: Main development branch. All new features and changes should be merged here first.  
- **hotfix**: For urgent bug fixes that need to be applied immediately to production.


## Git Commit Syntax

To keep commits clean and understandable, use the following syntax:

```
<type>(<scope>): <short description>
```


### Commit Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code changes that neither fix a bug nor add a feature
- **test**: Adding or updating tests
- **chore**: Changes to the build process or auxiliary tools

### Examples

```bash
git commit -m "feat(frontend): add responsive navbar"
git commit -m "fix(backend): correct user authentication error"
git commit -m "docs: update README with project structure"
git commit -m "style: fix indentation in main.css"
```