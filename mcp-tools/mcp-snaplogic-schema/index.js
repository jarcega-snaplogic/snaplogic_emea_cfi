#!/usr/bin/env node

/**
 * SnapLogic Schema MCP Server
 * Provides intelligent access to SnapLogic snap schemas with smart caching
 */

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import fetch from 'node-fetch';
import { readFileSync, existsSync } from 'fs';
import { SchemaCache } from './cache.js';

class SnapLogicSchemaServer {
  constructor() {
    this.cache = new SchemaCache();
    this.config = this.loadConfig();
    
    this.server = new Server(
      {
        name: 'snaplogic-schema',
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
    const configPath = '.snaplogic-config.json';
    if (existsSync(configPath)) {
      try {
        const config = JSON.parse(readFileSync(configPath, 'utf8'));
        return config;
      } catch (error) {
        // Silent fallback to default config
      }
    }
    
    // Fallback configuration
    return {
      credentials: {
        username: process.env.SNAPLOGIC_USERNAME || '',
        password: process.env.SNAPLOGIC_PASSWORD || ''
      },
      schema_api: {
        base_url: 'https://prodeu-connectfasterinc-cloud-fm.emea.snaplogic.io',
        org: 'ConnectFasterInc'
      }
    };
  }

  /**
   * Fetch snap catalog from SnapLogic API
   */
  async fetchCatalog() {
    if (this.cache.isLoading) {
      // Wait for existing load to complete
      while (this.cache.isLoading) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }
      return;
    }

    this.cache.isLoading = true;
    
    try {
      const url = `${this.config.schema_api.base_url}/api/1/rest/admin/snappack/catalog/snaps?org_path=/${this.config.schema_api.org}&level=detail`;
      
      const auth = Buffer.from(
        `${this.config.credentials.username}:${this.config.credentials.password}`
      ).toString('base64');

      const response = await fetch(url, {
        headers: {
          'Authorization': `Basic ${auth}`,
          'Accept': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      
      // Process and cache the catalog
      this.cache.processCatalog(data);
      
    } catch (error) {
      throw error;
    } finally {
      this.cache.isLoading = false;
    }
  }

  /**
   * Ensure catalog is loaded and fresh
   */
  async ensureCatalog() {
    if (this.cache.needsCatalogRefresh()) {
      await this.fetchCatalog();
    }
  }


  /**
   * Setup MCP tool handlers
   */
  setupToolHandlers() {
    this.server.setRequestHandler(ListToolsRequestSchema, async () => ({
      tools: [
        {
          name: 'search_snaps',
          description: 'Search for SnapLogic snaps by name or description',
          inputSchema: {
            type: 'object',
            properties: {
              query: {
                type: 'string',
                description: 'Search query for snap names or descriptions',
              },
              category: {
                type: 'string',
                description: 'Optional category filter (e.g., transform, flow, binary)',
              },
            },
            required: ['query'],
          },
        },
        {
          name: 'list_categories',
          description: 'List all available snap categories',
          inputSchema: {
            type: 'object',
            properties: {},
          },
        },
        {
          name: 'validate_snap_config',
          description: 'Validate a snap configuration against its schema',
          inputSchema: {
            type: 'object',
            properties: {
              config: {
                type: 'object',
                description: 'The snap configuration to validate',
              },
            },
            required: ['config'],
          },
        },
      ],
    }));

    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name, arguments: args } = request.params;

      try {
        // Ensure catalog is loaded for all operations
        await this.ensureCatalog();

        switch (name) {
          case 'search_snaps': {
            const query = args?.query;
            const category = args?.category;
            
            if (!query) {
              throw new Error('query is required');
            }

            const results = this.cache.searchSnaps(query, category);
            
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify(results, null, 2),
                },
              ],
            };
          }


          case 'list_categories': {
            const categories = this.cache.getCategories();
            
            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify(categories, null, 2),
                },
              ],
            };
          }

          case 'validate_snap_config': {
            const config = args?.config;
            
            if (!config) {
              throw new Error('config is required');
            }

            const validation = {
              valid: true,
              errors: [],
              warnings: []
            };

            // Basic validation
            if (!config.class_id) {
              validation.valid = false;
              validation.errors.push('Missing class_id');
            } else {
              const snapInfo = this.cache.getSnapInfo(config.class_id);
              if (!snapInfo) {
                validation.valid = false;
                validation.errors.push(`Unknown snap: ${config.class_id}`);
              } else {
                // Check version compatibility
                if (config.class_version && config.class_version !== snapInfo.version) {
                  validation.warnings.push(
                    `Version mismatch: config has ${config.class_version}, current is ${snapInfo.version}`
                  );
                }
              }
            }

            // Check required fields
            if (!config.instance_id) {
              validation.errors.push('Missing instance_id');
              validation.valid = false;
            }

            if (!config.property_map) {
              validation.errors.push('Missing property_map');
              validation.valid = false;
            }

            return {
              content: [
                {
                  type: 'text',
                  text: JSON.stringify(validation, null, 2),
                },
              ],
            };
          }

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
    // Don't log to stderr as it interferes with MCP communication
  }
}

const server = new SnapLogicSchemaServer();
console.error('[MCP] SnapLogic Schema Server starting...');
server.run().catch(() => process.exit(1));