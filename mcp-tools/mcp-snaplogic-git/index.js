#!/usr/bin/env node

/**
 * SnapLogic Git MCP Server
 * Provides Git operations for SnapLogic projects with GitHub integration
 */

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import fetch from 'node-fetch';
import { readFileSync, existsSync } from 'fs';

class SnapLogicGitServer {
  constructor() {
    this.config = this.loadConfig();
    
    this.server = new Server(
      {
        name: 'snaplogic-git',
        version: '1.0.0',
      },
      {
        capabilities: {
          tools: {},
        },
      }
    );

    this.setupToolHandlers();
  }

  /**
   * Load configuration from .snaplogic-config.json
   */
  loadConfig() {
    // Try multiple paths for config file
    const configPaths = [
      '.snaplogic-config.json',
      '../snaplogic_cicd_emea_cfi/.snaplogic-config.json',
      '/home/jocel/llmapps/snaplogic_cicd_emea_cfi/.snaplogic-config.json'
    ];
    
    for (const configPath of configPaths) {
      if (existsSync(configPath)) {
        try {
          const rawConfig = JSON.parse(readFileSync(configPath, 'utf8'));
          
          // Normalize config structure to match expected format
          const config = {
            credentials: {
              username: process.env.SNAPLOGIC_USERNAME || rawConfig.credentials?.username || '',
              password: process.env.SNAPLOGIC_PASSWORD || rawConfig.credentials?.password || ''
            },
            project_api: {
              base_url: rawConfig.api?.base_url || 'https://emea.snaplogic.com',
              org: rawConfig.api?.org || 'ConnectFasterInc',
              project_space: rawConfig.api?.project_space || 'tryGit',
              project_path: `snapLogic4snapLogic/${rawConfig.api?.project_space || 'tryGit'}`,
              project_id: rawConfig.api?.project_id || '855493'
            }
          };
          
          return config;
        } catch (error) {
          console.error(`Failed to parse config from ${configPath}:`, error);
          continue;
        }
      }
    }
    
    // Fallback configuration
    return {
      credentials: {
        username: process.env.SNAPLOGIC_USERNAME || '',
        password: process.env.SNAPLOGIC_PASSWORD || ''
      },
      project_api: {
        base_url: 'https://emea.snaplogic.com',
        org: 'ConnectFasterInc',
        project_space: 'tryGit',
        project_path: 'snapLogic4snapLogic/tryGit',
        project_id: '855493'
      }
    };
  }

  /**
   * Format GitHub authentication error with user-specific instructions
   */
  formatGitHubAuthError() {
    return {
      content: [{
        type: 'text',
        text: `GitHub authentication required. Please log into SnapLogic UI and authorize GitHub access for user: ${this.config.credentials.username}

Steps:
1. Go to SnapLogic Manager
2. Navigate to your project (${this.config.project_api.project_space})
3. Click "Authorize with GitHub" 
4. Complete OAuth flow
5. Try this operation again within 55 minutes

Note: GitHub authorization expires after ~55 minutes and must be renewed through the UI.`
      }],
      isError: true
    };
  }

  /**
   * Make authenticated request to SnapLogic API
   */
  async makeRequest(url, options = {}) {
    const auth = Buffer.from(
      `${this.config.credentials.username}:${this.config.credentials.password}`
    ).toString('base64');

    const response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Basic ${auth}`,
        'Content-Type': 'application/json',
        ...options.headers
      }
    });

    if (!response.ok) {
      const data = await response.json();
      
      // Check for GitHub auth error
      if (data.response_map?.error_id === 'github-auth-required') {
        return { githubAuthRequired: true };
      }
      
      throw new Error(
        data.response_map?.error_list?.[0]?.message || 
        `API error: ${response.status} ${response.statusText}`
      );
    }

    return response.json();
  }

  /**
   * Get Git repository status
   */
  async getGitStatus() {
    const url = `${this.config.project_api.base_url}/api/1/rest/public/project/repo-status/${this.config.project_api.org}/${this.config.project_api.project_path}`;
    
    const result = await this.makeRequest(url);
    
    if (result.githubAuthRequired) {
      return this.formatGitHubAuthError();
    }

    // Process the status response
    const entries = result.response_map?.entries || [];
    
    const status = {
      inSync: [],
      outOfSync: [],
      untracked: [],
      tracked: []
    };

    for (const entry of entries) {
      if (entry.metadata?.git) {
        const git = entry.metadata.git;
        
        if (git.status === 'untracked') {
          status.untracked.push({
            name: entry.name,
            path: entry.path,
            type: entry.asset_type
          });
        } else if (git.status === 'tracked') {
          status.tracked.push(entry.name);
          
          // Check if in sync
          if (git.current_sha && git.latest_repo_sha) {
            if (git.current_sha === git.latest_repo_sha) {
              status.inSync.push({
                name: entry.name,
                path: git.path,
                sha: git.current_sha
              });
            } else {
              status.outOfSync.push({
                name: entry.name,
                path: git.path,
                currentSha: git.current_sha,
                latestSha: git.latest_repo_sha
              });
            }
          }
        }
      }
    }

    // Format the response
    let text = `Git Sync Status for ${this.config.project_api.project_space} project:\n\n`;
    
    if (status.outOfSync.length > 0) {
      text += `Out of Sync (${status.outOfSync.length}):\n`;
      for (const item of status.outOfSync) {
        text += `- ${item.name}\n`;
        text += `  SnapLogic: ${item.currentSha.substring(0, 8)}...\n`;
        text += `  GitHub:    ${item.latestSha.substring(0, 8)}...\n\n`;
      }
    }
    
    text += `\nIn Sync: ${status.inSync.length} assets\n`;
    text += `Tracked: ${status.tracked.length} total\n`;
    text += `Untracked: ${status.untracked.length} assets`;

    return {
      content: [{
        type: 'text',
        text
      }]
    };
  }

  /**
   * Pull changes from GitHub
   */
  async gitPull() {
    const url = `${this.config.project_api.base_url}/api/1/rest/public/project/pull/${this.config.project_api.org}/${this.config.project_api.project_path}`;
    
    const result = await this.makeRequest(url, { method: 'POST' });
    
    if (result.githubAuthRequired) {
      return this.formatGitHubAuthError();
    }

    // Process the pull response
    const responseMap = result.response_map || {};
    
    let text = 'Git Pull Result:\n\n';
    
    if (responseMap.updated_assets?.length > 0) {
      text += `Updated ${responseMap.updated_assets.length} assets:\n`;
      for (const asset of responseMap.updated_assets) {
        text += `- ${asset}\n`;
      }
    } else {
      text += 'No updates needed - all assets are already in sync.\n';
    }
    
    if (responseMap.errors?.length > 0) {
      text += `\nErrors:\n`;
      for (const error of responseMap.errors) {
        text += `- ${error}\n`;
      }
    }

    return {
      content: [{
        type: 'text',
        text
      }]
    };
  }

  setupToolHandlers() {
    this.server.setRequestHandler(ListToolsRequestSchema, async () => ({
      tools: [
        {
          name: 'git_status',
          description: 'Check Git sync status between SnapLogic and GitHub repository',
          inputSchema: {
            type: 'object',
            properties: {},
          },
        },
        {
          name: 'git_pull',
          description: 'Pull latest changes from GitHub repository to SnapLogic project',
          inputSchema: {
            type: 'object',
            properties: {},
          },
        },
      ],
    }));

    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name } = request.params;

      try {
        switch (name) {
          case 'git_status':
            return await this.getGitStatus();
            
          case 'git_pull':
            return await this.gitPull();
            
          default:
            throw new Error(`Unknown tool: ${name}`);
        }
      } catch (error) {
        return {
          content: [
            {
              type: 'text',
              text: `Error: ${error instanceof Error ? error.message : String(error)}`,
            },
          ],
          isError: true,
        };
      }
    });
  }

  async run() {
    const transport = new StdioServerTransport();
    await this.server.connect(transport);
    console.error('SnapLogic Git MCP server running on stdio');
  }
}

const server = new SnapLogicGitServer();
server.run().catch(console.error);