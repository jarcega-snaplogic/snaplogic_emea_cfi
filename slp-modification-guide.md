# SnapLogic .slp File Modification Guide

## Overview

This comprehensive guide provides the technical knowledge and practical procedures needed to safely modify SnapLogic pipeline (.slp) files directly. This documentation combines theoretical understanding of the .slp format with concrete examples from real-world RFP automation pipelines.

**Target Audience**: Technical personnel with JSON experience who need to programmatically modify SnapLogic pipelines

**When to Use This Guide**: 
- Bulk modifications that are impractical through the UI
- Environment-specific configuration management
- Programmatic pipeline generation
- CI/CD pipeline automation
- Complex property modifications (hundreds of parameters)

## .slp File Architecture

### Core Principle: Normalized Relational Model
The .slp file uses a normalized structure similar to a relational database. Objects reference each other via unique IDs rather than nested hierarchies. This minimizes redundancy but requires coordinated updates across multiple JSON objects.

### Top-Level Structure
```json
{
    "ai_map": {
        "footprint": "H4sIAAAAAAAAA8srzckBAE/8yyUEAAAA"
    },
    "class_id": "com-snaplogic-pipeline",
    "class_version": 9,
    "link_map": { /* Snap connections - the data flow graph */ },
    "property_map": { /* Pipeline-level configuration */ },
    "render_map": { /* Visual layout information */ },
    "snap_map": { /* Individual snap definitions and configurations */ }
}
```

## JSON Object Reference

### 1. link_map - Pipeline Data Flow
Defines directed connections between snaps, creating the pipeline's execution graph.

**Structure Pattern**:
```json
"link_map": {
    "link134": {
        "dst_id": "11111111-1111-1111-1111-000000000014",
        "dst_view_id": "input0",
        "src_id": "11111111-1111-1111-1111-000000000012",
        "src_view_id": "output0"
    }
}
```

**Key Rules**:
- Link IDs follow pattern `link###` (incremental numbers)
- `src_id` and `dst_id` must exist in `snap_map`
- `src_view_id` typically `output0`, `output1`, etc.
- `dst_view_id` typically `input0`, `input1`, etc.
- Each connection represents one data flow path

### 2. property_map - Pipeline Configuration
Contains pipeline-level settings including parameters, error handling, and metadata.

**Critical Sections**:
```json
"property_map": {
    "settings": {
        "param_table": {
            "value": [
                {
                    "key": { "value": "question" },
                    "data_type": { "value": "string" },
                    "description": { "value": "RFP question parameter" },
                    "required": { "value": true },
                    "capture": { "value": false }
                }
            ]
        },
        "error_pipeline": {
            "expression": false,
            "value": null
        }
    },
    "info": {
        "author": { "value": "user@company.com" },
        "label": {},
        "notes": { "value": "Pipeline description" }
    }
}
```

### 3. snap_map - Snap Definitions
The core object containing all snap configurations. Each snap has a unique GUID and complete configuration.

**Structure Pattern**:
```json
"snap_map": {
    "11111111-1111-1111-1111-000000000003": {
        "class_id": "com-snaplogic-snaps-azureopenai-azureopenaipromptgenerator",
        "class_version": 1,
        "instance_id": "11111111-1111-1111-1111-000000000003",
        "property_map": {
            "account": { /* Account reference if needed */ },
            "error": { /* Error handling configuration */ },
            "info": { /* Snap metadata */ },
            "input": { /* Input view definitions */ },
            "output": { /* Output view definitions */ },
            "settings": { /* Main snap configuration */ }
        },
        "view_map": { /* Input/output view configurations */ }
    }
}
```

## Snap-Specific Configuration Patterns

### Azure OpenAI Prompt Generator
**Class ID**: `com-snaplogic-snaps-azureopenai-azureopenaipromptgenerator`

**Key Configuration**:
```json
"settings": {
    "advancedMode": { "value": true },
    "content": { "expression": true, "value": null },
    "editable_content": {
        "value": "You are the Retrieval Sub-Agent responsible for finding accurate information to answer RFP questions..."
    },
    "role": { "value": "SYSTEM" },
    "promptTemplate": { "value": "Context Q & A" }
}
```

**Modification Points**:
- `editable_content.value`: System prompt text
- `role.value`: USER, SYSTEM, or ASSISTANT
- `promptTemplate.value`: Template type selection

### AWS Bedrock Function Generator
**Class ID**: `com-snaplogic-snaps-awsbedrock-awsconversefunctiongenerator`

**Key Configuration**:
```json
"settings": {
    "name": { "value": "RetrieverAnalystRFIsApi" },
    "description": { "value": "Searches analyst reports from Gartner, Aragon Research" },
    "parameters": {
        "value": [
            {
                "paramName": { "value": "Requirement" },
                "paramType": { "value": "STRING" },
                "paramDescription": { "value": "An RFX Question about snapLogic" },
                "paramRequired": { "value": true }
            }
        ]
    },
    "advancedToolConfiguration": {
        "value": {
            "toolChoice": { "value": "SPECIFY A FUNCTION" }
        }
    }
}
```

**Modification Points**:
- `name.value`: Function name (maps to API endpoint)
- `description.value`: Function description for AI model
- `parameters.value[]`: Array of function parameters
- Each parameter has: `paramName`, `paramType`, `paramDescription`, `paramRequired`

### APIM Function Generator (Multiple APIs)
**Class ID**: `com-snaplogic-snaps-llmutils-apimfunctiongenerator`

**Key Configuration**:
```json
"settings": {
    "baseUrl": { "value": "https://prodeu-connectfasterinc-cloud-fm.emea.snaplogic.io/service/snapLogic4snapLogic/6.0" },
    "projectPath": { "value": "snapLogic4snapLogic/ToolsAsApi" },
    "serviceName": { "value": "Tools" },
    "paths": {
        "value": [
            {
                "method": { "value": "POST" },
                "path": { "value": "/RetrieverAnalystRFIsApi" }
            },
            {
                "method": { "value": "POST" },
                "path": { "value": "/RetrieverDocumentationApi" }
            }
        ]
    },
    "tags": {
        "value": [
            { "tag": { "value": "Search & Retrieval - analyst" } }
        ]
    }
}
```

**Modification Points**:
- `baseUrl.value`: API base URL (environment-specific)
- `paths.value[]`: Array of API endpoints to expose
- `tags.value[]`: Categorization tags

### HTTP Client Snap
**Class ID**: `com-snaplogic-snaps-httpclient-get`, `com-snaplogic-snaps-httpclient-post`

**Key Configuration**:
```json
"settings": {
    "http_entity": {
        "expression": true,
        "value": "JSON.stringify($)"
    },
    "request_headers": {
        "value": [
            {
                "key": { "value": "Content-Type" },
                "value": { "value": "application/json" }
            }
        ]
    },
    "service_url": {
        "expression": true,
        "value": "_baseUrl + '/RetrieverAnalystRFIsApi'"
    }
}
```

## Account Reference Patterns

Account references follow a consistent structure across all snaps that require external system access.

### Standard Account Reference Structure
```json
"account": {
    "account_ref": {
        "value": {
            "label": {
                "expression": false,
                "label": "shared/azure openai ja",
                "value": "azure openai ja"
            },
            "ref_class_id": { "value": "com-snaplogic-snaps-azureopenai-azureopenaiapikeyaccount" },
            "ref_id": { "expression": false, "value": null }
        }
    }
}
```

### Common Account Types
| Account Type | ref_class_id | Usage |
|--------------|--------------|-------|
| Azure OpenAI | `com-snaplogic-snaps-azureopenai-azureopenaiapikeyaccount` | AI model access |
| MongoDB Atlas | `com-snaplogic-snaps-mongo-replicasetmongoaccount` | Data logging |
| HTTP OAuth2 | `com-snaplogic-snaps-apisuite-accounts-httpoauth2account` | API authentication |
| Snowflake | `com-snaplogic-snap-api-sql-accounts-snowflakedatabaseaccount` | Data warehousing |

### Environment-Specific Account Handling
**Critical**: Account `ref_id` values are environment-specific and must be updated when moving between dev/test/prod.

## Expression System

SnapLogic uses a dual-mode system for properties: static values and dynamic expressions.

### Static Values
```json
"paramName": {
    "expression": false,
    "value": "RetrieverAnalystRFIsApi"
}
```

### Dynamic Expressions
```json
"pathExpr": {
    "expression": true,
    "value": "'sldb:///%s-%s-%s.json'.sprintf(encodeURIComponent(pipe.label), encodeURIComponent(snap.label), snap.instanceId)"
}
```

### Common Expression Patterns
- **Pipeline parameters**: `_question` (underscore prefix)
- **Snap references**: `snap.label`, `snap.instanceId`
- **Pipeline metadata**: `pipe.label`
- **Data access**: `$field_name`, `$.path.to.data`
- **String formatting**: `'template %s'.sprintf(value)`
- **JSON operations**: `JSON.stringify($)`, `JSON.parse(expression)`

## Practical Modification Procedures

### Safe Modification Workflow

#### 1. Pre-Modification Setup
```bash
# Export pipeline from SnapLogic Manager
# Backup original file
cp pipeline.slp pipeline.slp.backup

# Format JSON for readability (if jq available)
jq '.' pipeline.slp > pipeline_formatted.slp

# Or use code editor to format JSON
```

#### 2. Modification Process
1. **Open in JSON-aware editor** (VS Code, IntelliJ, etc.)
2. **Validate syntax** continuously during editing
3. **Maintain ID relationships** - ensure all references are valid
4. **Follow expression patterns** - proper `"expression": true/false` flags

#### 3. Validation Steps
```bash
# Validate JSON syntax
python -m json.tool pipeline.slp > /dev/null

# Or with Node.js
node -e "JSON.parse(require('fs').readFileSync('pipeline.slp', 'utf8'))"
```

#### 4. Import and Test
1. **Import to development environment first**
2. **Validate in SnapLogic Designer**
3. **Test execution with sample data**
4. **Monitor execution logs**

### Common Modification Scenarios

#### Scenario 1: Adding a New Retrieval Tool

**Step 1**: Add Function Generator Snap
```json
"11111111-1111-1111-1111-000000000099": {
    "class_id": "com-snaplogic-snaps-awsbedrock-awsconversefunctiongenerator",
    "class_version": 1,
    "instance_id": "11111111-1111-1111-1111-000000000099",
    "property_map": {
        "settings": {
            "name": { "value": "RetrieverNewDataSourceApi" },
            "description": { "value": "Searches new data source for relevant information" },
            "parameters": {
                "value": [
                    {
                        "paramName": { "value": "Query" },
                        "paramType": { "value": "STRING" },
                        "paramDescription": { "value": "Search query" },
                        "paramRequired": { "value": true }
                    }
                ]
            }
        }
    }
}
```

**Step 2**: Add to APIM Function Generator paths
```json
"paths": {
    "value": [
        // existing paths...
        {
            "method": { "value": "POST" },
            "path": { "value": "/RetrieverNewDataSourceApi" }
        }
    ]
}
```

**Step 3**: Update link_map connections
```json
"link999": {
    "dst_id": "11111111-1111-1111-1111-000000000099",
    "dst_view_id": "input0",
    "src_id": "11111111-1111-1111-1111-000000000098",
    "src_view_id": "output0"
}
```

#### Scenario 2: Environment Migration (Dev → Prod)

**Account Reference Updates**:
```python
# Python script example for account ID updates
import json

def update_account_references(slp_file, account_mapping):
    with open(slp_file, 'r') as f:
        pipeline = json.load(f)
    
    # Update account references in snap_map
    for snap_id, snap_config in pipeline.get('snap_map', {}).items():
        account_config = snap_config.get('property_map', {}).get('account', {})
        if account_config:
            account_ref = account_config.get('account_ref', {}).get('value', {})
            if 'label' in account_ref:
                old_label = account_ref['label']['value']
                if old_label in account_mapping:
                    account_ref['label']['value'] = account_mapping[old_label]
                    account_ref['label']['label'] = f"shared/{account_mapping[old_label]}"
    
    # Save updated pipeline
    with open(f"{slp_file}.prod", 'w') as f:
        json.dump(pipeline, f, indent=2)

# Usage
account_mapping = {
    "azure openai dev": "azure openai prod",
    "mongodb atlas dev": "mongodb atlas prod"
}
update_account_references('pipeline.slp', account_mapping)
```

#### Scenario 3: Bulk Prompt Updates

**Update AI Agent Prompts**:
```python
def update_ai_prompts(slp_file, prompt_updates):
    with open(slp_file, 'r') as f:
        pipeline = json.load(f)
    
    for snap_id, snap_config in pipeline.get('snap_map', {}).items():
        class_id = snap_config.get('class_id', '')
        
        # Update Azure OpenAI prompts
        if 'azureopenaipromptgenerator' in class_id:
            settings = snap_config.get('property_map', {}).get('settings', {})
            if 'editable_content' in settings:
                old_prompt = settings['editable_content']['value']
                for old_text, new_text in prompt_updates.items():
                    if old_text in old_prompt:
                        settings['editable_content']['value'] = old_prompt.replace(old_text, new_text)
        
        # Update AWS Bedrock function descriptions
        elif 'awsconversefunctiongenerator' in class_id:
            settings = snap_config.get('property_map', {}).get('settings', {})
            if 'description' in settings:
                old_desc = settings['description']['value']
                for old_text, new_text in prompt_updates.items():
                    if old_text in old_desc:
                        settings['description']['value'] = old_desc.replace(old_text, new_text)
    
    with open(f"{slp_file}.updated", 'w') as f:
        json.dump(pipeline, f, indent=2)
```

## Risk Management

### Risk Categories and Mitigation

#### 1. Syntactic Risk
**Risk**: JSON structure corruption
**Examples**: Missing commas, unmatched brackets, improper escaping
**Mitigation**:
- Use JSON-aware editors with real-time validation
- Run syntax validation before import
- Use automated formatting tools

#### 2. Semantic Risk  
**Risk**: Valid JSON with incorrect SnapLogic logic
**Examples**: Wrong data types, invalid expressions, mismatched field references
**Mitigation**:
- Validate in SnapLogic Designer before production use
- Test with representative data sets
- Peer review of modifications

#### 3. Dependency Risk
**Risk**: References to non-existent assets
**Examples**: Invalid account IDs, missing child pipelines, broken file paths
**Mitigation**:
- Use API validation scripts to verify dependencies
- Maintain environment-specific asset inventories
- Implement automated dependency checking in CI/CD

#### 4. Performance Risk
**Risk**: Modifications that degrade performance or stability
**Examples**: Removed sorting before grouping, incorrect pagination settings
**Mitigation**:
- Performance testing in production-like environments
- Monitor resource utilization
- Follow SnapLogic performance best practices

### Three Gates Validation Framework

#### Gate 1: Code Review
- **Version Control**: All changes in Git with feature branches
- **Peer Review**: Mandatory pull request review
- **JSON Formatting**: Prettified for meaningful diffs

#### Gate 2: Automated Validation
- **Syntax Check**: Automated JSON validation
- **Dependency Check**: API-based verification of external references
- **Schema Validation**: Confirm snap class IDs and property structures

#### Gate 3: Execution Testing
- **Development Testing**: Deploy and test in dev environment
- **Smoke Tests**: Automated execution with test data
- **Monitoring**: Resource utilization and error monitoring

## Environment Configuration Management

### Strategy Comparison

| Approach | Complexity | Flexibility | Security | Best For |
|----------|------------|-------------|----------|----------|
| Pipeline Parameters | Low | Low | Medium | Simple configurations |
| Expression Libraries | Medium | Medium | Medium | Centralized config values |
| API-based Updates | High | High | High | Enterprise environments |

### Pipeline Parameters Approach
```json
"param_table": {
    "value": [
        {
            "key": { "value": "api_base_url" },
            "data_type": { "value": "string" },
            "description": { "value": "Environment-specific API base URL" },
            "required": { "value": true }
        }
    ]
}
```

Usage in snaps:
```json
"service_url": {
    "expression": true,
    "value": "_api_base_url + '/RetrieverAnalystRFIsApi'"
}
```

### Expression Libraries Approach
Create environment-specific `.expr` files:
```javascript
// config.prod.expr
function getApiBaseUrl() {
    return "https://prod-api.company.com/service";
}

function getDatabaseName() {
    return "prod_database";
}
```

Usage in pipeline:
```json
"service_url": {
    "expression": true,
    "value": "getApiBaseUrl() + '/RetrieverAnalystRFIsApi'"
}
```

## Quick Reference Tables

### Snap Class IDs for RFP Automation
| Snap Type | Class ID | Primary Use |
|-----------|----------|-------------|
| Azure OpenAI Prompt Generator | `com-snaplogic-snaps-azureopenai-azureopenaipromptgenerator` | System prompt creation |
| AWS Bedrock Function Generator | `com-snaplogic-snaps-awsbedrock-awsconversefunctiongenerator` | Tool definition |
| APIM Function Generator | `com-snaplogic-snaps-llmutils-apimfunctiongenerator` | Multi-API exposure |
| HTTP Client Get | `com-snaplogic-snaps-httpclient-get` | API calls |
| Structural Transform | `com-snaplogic-snaps-llmutils-structuraltransform` | Data restructuring |
| MongoDB Insert | `com-snaplogic-snaps-mongo-insert` | Logging |
| Agent Visualizer | `com-snaplogic-snaps-llmutils-agentvisualizer` | Conversation tracking |

### Common Property Paths
| Configuration | JSON Path |
|---------------|-----------|
| Snap Label | `snap_map.[snap_id].property_map.info.label.value` |
| Account Reference | `snap_map.[snap_id].property_map.account.account_ref.value.label.value` |
| Main Settings | `snap_map.[snap_id].property_map.settings` |
| Pipeline Parameters | `property_map.settings.param_table.value` |
| Error Pipeline | `property_map.settings.error_pipeline.value` |

### Expression Flags
| Context | Expression Flag | Example Value |
|---------|----------------|---------------|
| Static string | `"expression": false` | `"RetrieverAnalystRFIsApi"` |
| Pipeline parameter | `"expression": true` | `"_question"` |
| Data field access | `"expression": true` | `"$.messages"` |
| Function call | `"expression": true` | `"JSON.stringify($)"` |
| Template string | `"expression": true` | `"'API: %s'.sprintf(_api_name)"` |

## Best Practices Summary

### Development Practices
1. **Always backup original files** before modification
2. **Use version control** for all pipeline modifications
3. **Format JSON** for human readability and meaningful diffs
4. **Validate syntax** before attempting import
5. **Test in development** environment first

### Modification Practices
1. **Maintain ID consistency** - ensure all references are valid
2. **Follow expression patterns** - proper syntax and flags
3. **Update coordinated objects** - snap_map, link_map, property_map
4. **Preserve account structure** - don't modify account reference format
5. **Document changes** - maintain change logs for complex modifications

### Production Practices
1. **Implement Three Gates** validation framework
2. **Use environment-specific** configurations
3. **Monitor performance** after modifications
4. **Maintain rollback capability** - keep working versions
5. **Automate repetitive tasks** - scripted modifications for bulk changes

This guide provides the foundation for safe, effective .slp file modifications. Always prioritize safety and validation over speed, and maintain comprehensive testing practices for any production changes.