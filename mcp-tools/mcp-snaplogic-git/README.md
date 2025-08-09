# SnapLogic Git MCP Server

MCP server for SnapLogic Git operations with GitHub integration. Provides tools to check Git sync status and pull changes from GitHub to SnapLogic projects.

## Features

- **git_status**: Check sync status between SnapLogic and GitHub repository
- **git_pull**: Pull latest changes from GitHub repository to SnapLogic project
- **Smart Error Handling**: Detects GitHub authentication issues and provides clear re-authorization instructions

## Prerequisites

1. **SnapLogic Account**: Valid credentials for SnapLogic platform
2. **GitHub Integration**: SnapLogic project must be connected to a GitHub repository
3. **Configuration File**: `.snaplogic-config.json` in MCP tool directory

## Configuration

Create `.snaplogic-config.json` in the MCP tool directory:

```json
{
  "credentials": {
    "username": "your-email@domain.com",
    "password": "your-snaplogic-password"
  },
  "project_api": {
    "base_url": "https://emea.snaplogic.com",
    "org": "YourOrganization",
    "project_space": "YourProjectSpace",
    "project_path": "YourOrg/YourProject"
  }
}
```

**Note**: This follows the same pattern as the schema MCP tool - the config file should be in the tool's directory, not the project root.

## Installation

```bash
cd mcp-tools/mcp-snaplogic-git
npm install
```

## Usage

### As MCP Server

Add to your Claude Code MCP settings (`~/.claude/mcp_servers.json`):

```json
{
  "snaplogic-git": {
    "command": "node",
    "args": ["index.js"],
    "cwd": "/home/jocel/llmapps/vibe-coding-snaplogic-new/mcp-tools/mcp-snaplogic-git"
  }
}
```

### Available Tools

#### git_status

Check sync status between SnapLogic and GitHub:

```
git_status
```

Returns:
- Assets that are out of sync (SHA mismatches)
- In-sync asset count
- Untracked assets
- Clear GitHub re-authorization instructions if needed

#### git_pull

Pull changes from GitHub to SnapLogic:

```
git_pull
```

Returns:
- List of updated assets
- Success/failure status
- Error details if any issues occur

## GitHub Authentication

SnapLogic's GitHub integration requires periodic re-authorization through the UI:

1. **Session Duration**: ~55 minutes
2. **Re-authorization Steps**:
   - Go to SnapLogic Manager
   - Navigate to your project
   - Click "Authorize with GitHub"
   - Complete OAuth flow

The tools automatically detect authentication issues and provide user-specific instructions.

## Error Handling

The server handles common scenarios gracefully:

- **Network errors**: Clear error messages
- **Authentication failures**: Specific re-auth instructions
- **Invalid configurations**: Helpful troubleshooting info
- **API errors**: Detailed error context

## Workflow Integration

Designed for CI/CD workflows:

1. **Local Development**: Create/modify .slp files locally
2. **Git Commit**: Push changes to GitHub repository
3. **Sync to SnapLogic**: Use `git_pull` to update SnapLogic project
4. **Status Monitoring**: Use `git_status` to detect conflicts

## Limitations

- **Manual GitHub Auth**: No programmatic way to authenticate with GitHub
- **Individual User Accounts**: Service accounts cannot be used for Git operations
- **Session Timeouts**: GitHub authorization expires and requires UI re-authorization

## Troubleshooting

### "GitHub authentication required" error

The GitHub OAuth session has expired. Follow the re-authorization steps provided in the error message.

### "API error" messages

Check your SnapLogic credentials and network connectivity. Ensure the project configuration matches your SnapLogic setup.

### Configuration not found

Ensure `.snaplogic-config.json` exists in the project root with valid credentials and project settings.

## Development

### Running Tests

```bash
npm start
```

### Architecture

- **ES Modules**: Modern JavaScript module system
- **MCP SDK**: Standard Model Context Protocol implementation
- **node-fetch**: HTTP client for SnapLogic API calls
- **Configuration-driven**: No hardcoded values

## Support

For issues related to:
- **SnapLogic APIs**: Contact SnapLogic support
- **MCP Integration**: Check Model Context Protocol documentation
- **GitHub Integration**: Refer to SnapLogic Git integration docs