# MCP Development Best Practices

## Overview
This document captures the key learnings from developing and deploying Model Context Protocol (MCP) servers for SnapLogic CI/CD integration, based on real-world troubleshooting and successful deployment.

## Key Success Factor: Absolute Paths

### Problem Encountered
**Path Resolution Issues**: Relative paths in MCP configuration caused "Cannot find module" errors when Claude Code attempted to start MCP servers.

**Root Cause**: MCP servers are launched from the current working directory of Claude Code, not from the directory containing the `.mcp.json` file. This means relative paths like `./vibe-coding-snaplogic-new/mcp-tools/mcp-snaplogic-git/index.js` are resolved from the wrong location.

**Error Pattern**:
```
Error: Cannot find module '/home/jocel/llmapps/snaplogic_cicd_emea_cfi/vibe-coding-snaplogic-new/mcp-tools/mcp-snaplogic-git/index.js'
```

**Solution**: Use absolute paths in `.mcp.json` configuration:

```json
{
  "mcpServers": {
    "snaplogic-git": {
      "command": "node", 
      "args": ["/home/jocel/llmapps/vibe-coding-snaplogic-new/mcp-tools/mcp-snaplogic-git/index.js"],
      "env": {
        "SNAPLOGIC_USERNAME": "jarcega@snaplogic.com",
        "SNAPLOGIC_PASSWORD": "ORJAcon_01"
      }
    }
  }
}
```

## Architecture Patterns

### 1. Configuration Management
- **Environment Variables**: Use MCP server env configuration for credentials
- **Local Config Files**: Use `.snaplogic-config.json` for project-specific settings
- **Working Directory**: MCP tools should read config files relative to their execution context

### 2. File Structure
```
project-root/
├── .mcp.json                    # MCP server configuration
├── .claude/
│   └── settings.local.json      # Claude Code permissions
├── .snaplogic-config.json       # Project configuration
└── (other project files)

development-repo/
└── mcp-tools/
    ├── mcp-snaplogic-schema/
    │   ├── index.js
    │   └── package.json
    └── mcp-snaplogic-git/
        ├── index.js
        └── package.json
```

### 3. Error Handling Best Practices
- **Graceful Degradation**: Handle missing configuration files elegantly
- **User-Friendly Messages**: Provide actionable error messages with specific user instructions
- **Authentication Context**: Include username in GitHub auth error messages

## Troubleshooting Workflow

### 1. Connection Issues
1. **Check MCP Logs**: Look in `~/.cache/claude-cli-nodejs/-home-...-project-name/mcp-logs-server-name/`
2. **Verify File Paths**: Ensure absolute paths exist and files have proper permissions
3. **Test Node Execution**: Run MCP server directly with `node /absolute/path/to/index.js`

### 2. Configuration Issues
1. **Environment Variables**: Verify credentials are properly passed to MCP server
2. **Config File Location**: Ensure `.snaplogic-config.json` is in the correct working directory
3. **Path Resolution**: Check if MCP tool is reading config from expected location

### 3. Permission Issues
1. **File Permissions**: Ensure MCP server files are readable/executable
2. **Claude Permissions**: Add MCP tool permissions to `.claude/settings.local.json`
3. **Environment Access**: Verify MCP server can access environment variables

## Configuration Templates

### .mcp.json Template
```json
{
  "mcpServers": {
    "server-name": {
      "command": "node",
      "args": ["/absolute/path/to/mcp-tool/index.js"],
      "env": {
        "SNAPLOGIC_USERNAME": "user@example.com",
        "SNAPLOGIC_PASSWORD": "password"
      }
    }
  }
}
```

### Claude Settings Template
```json
{
  "$schema": "https://json.schemastore.org/claude-code-settings.json",
  "permissions": {
    "allow": [
      "mcp__server-name__tool_name"
    ]
  },
  "enabledMcpjsonServers": [
    "server-name"
  ]
}
```

## Common Pitfalls

### 1. Path Resolution
- **❌ Wrong**: `"args": ["./relative/path/to/index.js"]`
- **✅ Correct**: `"args": ["/absolute/path/to/index.js"]`

### 2. Configuration File Access
- **Issue**: MCP server reads config from wrong directory
- **Solution**: Use process.cwd() or explicit path resolution in MCP server code

### 3. Environment Variable Scope
- **Issue**: MCP server doesn't receive environment variables from .mcp.json
- **Solution**: Ensure env variables are properly defined in MCP configuration

## Success Indicators

### MCP Server Connection
- **Positive**: No connection errors in MCP logs
- **Positive**: Tools appear in `/mcp` command output
- **Positive**: Tools execute without "Cannot read properties" errors

### Configuration Loading
- **Positive**: MCP server can read base_url, credentials, and project settings
- **Positive**: Error messages are specific and actionable
- **Positive**: GitHub authentication errors include user context

## Future Improvements

1. **Configuration Validation**: Add startup validation for required config fields
2. **Path Flexibility**: Support both relative and absolute paths with smart resolution
3. **Environment Detection**: Auto-detect project configuration file locations
4. **Diagnostic Tools**: Build in self-diagnostic capabilities for MCP servers

## Key Takeaway
**Absolute paths solve 90% of MCP deployment issues.** When MCP servers fail to start, check the path resolution first before investigating complex configuration problems.