# CLAUDE.md - SnapLogic Environment Guide

This file provides guidance to Claude Code (claude.ai/code) when working with SnapLogic pipelines and assets in this repository.

## Repository Overview

This repository contains SnapLogic integration pipelines and data assets for various business automation use cases including AI-powered RFP processing, employee onboarding workflows, and data transformation pipelines. The environment leverages SnapLogic's iPaaS capabilities with advanced AI/ML integrations.

## SnapLogic Environment Configuration

### Platform Details
- **Environment**: SnapLogic EMEA Cloud (ConnectFasterInc)
- **Base URL**: `https://prodeu-connectfasterinc-cloud-fm.emea.snaplogic.io`
- **Project Context**: Project ID 1126549, "sandbox 3" environment
- **Author**: jarcega@snaplogic.com

### Available Tools & Resources
1. **MCP SnapLogic Schema Tool**: Provides access to snap schemas, validation, and template generation
2. **SnapLogic Documentation**: 
   - Snap Reference: https://docs-snaplogic.atlassian.net/wiki/spaces/SD/pages/1439259/Snap+Reference
   - Snap Overview: https://docs.snaplogic.com/snaps/snaps-about.html
3. **Web Search**: For additional snap configuration examples and best practices

## Pipeline Assets & Architecture

### Core Business Pipelines

#### 1. AI-Powered RFP Automation System
**Primary Use Case**: Automated response generation for Request for Proposal questionnaires

**Pipeline Components**:
- `AutoRFPPreProcessing.slp` - Data preprocessing using AWS Bedrock (Claude Sonnet 4)
- `AgentDriverAutoRFPAnalystRFIs.slp` - Main orchestrator with compliance processing
- `AgentDriverAutoRFPRefiner.slp` - RFP refinement coordination
- `AgentWorkerAutoRFPRefiner.slp` - Multi-search worker with parallel execution
- `SubAgentDriverAnswerFinder.slp` - Specialized retrieval agent with intelligent routing
- `ApiTriggerLoopioAnalystRfi.slp` - Loopio platform integration and data warehousing

**Architecture Pattern**: Hierarchical Agent System
- **Driver Agents**: Workflow coordination and decision-making
- **Worker Agents**: Task-specific processing and iterative refinement
- **Sub-Agents**: Information retrieval and content analysis

**Key Integrations**:
- **Loopio Platform**: RFP management and compliance tracking
- **MongoDB Atlas**: Session logging and performance analytics
- **Snowflake**: Data warehousing for business intelligence
- **Multiple AI Models**: AWS Bedrock, Azure OpenAI with function calling

#### 2. Employee Onboarding Automation
**Primary Use Case**: Streamlined employee onboarding workflow automation

**Pipeline Components**:
- `AgentDriverEmployeeOnboarding.slp` - Comprehensive onboarding orchestration

**Features**:
- Multi-system integration for HR processes
- Automated documentation and compliance tracking
- Workflow coordination across departments

#### 3. Text-to-Speech Integration
**Primary Use Case**: AI-generated audio content creation

**Pipeline Components**:
- `TextToSpeechElevenLabs.slp` - ElevenLabs TTS integration
- `TextToSpeechElevenLabs-MCPGenerated.slp` - MCP-generated variant

**Architecture**:
- JSON payload generation for API requests
- HTTP client integration with ElevenLabs API
- Binary audio file processing and storage

#### 4. CSV Data Processing
**Primary Use Case**: Sample data generation and file export

**Pipeline Components**:
- `CSVDataPipeline.slp` - Complete CSV processing workflow

**Data Flow**:
- CSV Generator → CSV Formatter → File Writer
- Schema-validated snap configurations
- Configurable output paths and data formats

### Retrieval & Search System

**API Base Path**: `/snapLogic4snapLogic/ToolsAsApi/Tools/6.0/`

**Primary Data Sources** (Priority Order):
1. **RetrieverAnalystRFIsApi** - Gartner/Aragon analyst reports (Highest Priority)
2. **RetrieverDocumentationApi** - Official technical documentation
3. **RetrieverLoopioTechApi** - Curated RFP knowledge base
4. **RetrieverSigmaFrameworkApi** - Enterprise security frameworks
5. **RetrieverTechnicalBlogApi** - Implementation guides and tutorials
6. **RetrieverSlackCrawlerApi** - Internal knowledge mining
7. **RetrieverCuratedRfiIndexApi** - Historical RFP response library

**Decision Logic**:
- **Analyst Score ≥25**: Use RetrieverAnalystRFIsApi exclusively
- **Analyst Score <25**: Execute all 6 retrievers in parallel
- **Fallback**: AskSnapGptApi when all scores ≤25

**Quality Scoring System**:
- **Relevance** (0-10): Content alignment with query
- **Completeness** (0-10): Depth and coverage of information
- **Authority** (0-10): Source credibility and accuracy
- **Total Score**: 0-30 points

## SnapLogic Development Workflows

### 1. Pipeline Analysis & Documentation

**Process**:
1. **Asset Discovery**: Use `Glob` and `Read` tools to examine existing .slp files
2. **Schema Analysis**: Leverage MCP tool to understand snap configurations
3. **Relationship Mapping**: Document data flow and integration patterns
4. **Use Case Identification**: Extract business logic and workflow purposes

**Example Commands**:
```bash
# Find all pipeline files
find . -name "*.slp" -type f

# Analyze pipeline structure
grep -E "(class_id|snap_map)" pipeline.slp
```

### 2. Pipeline Creation & Modification

**Development Steps**:

#### Step 1: Requirements Analysis
- Identify business use case and data flow requirements
- Review existing pipelines for similar patterns
- Document required snap types and configurations

#### Step 2: Schema Validation
- Use MCP SnapLogic Schema Tool to get accurate snap schemas:
  ```
  mcp__snaplogic-schema__search_snaps(query="snap_name")
  mcp__snaplogic-schema__get_snap_schema(class_id="com-snaplogic-snaps-...")
  mcp__snaplogic-schema__generate_snap_template(class_id="...")
  ```

#### Step 3: Configuration Research
- Search SnapLogic documentation for snap-specific configuration options
- Use web search for implementation examples and best practices
- Review existing pipeline configurations for similar use cases

#### Step 4: Pipeline Construction
- Build pipeline JSON structure following SnapLogic schema
- Configure snap properties based on validated schemas
- Implement proper error handling and execution modes
- Set up pipeline parameters for flexibility

#### Step 5: Validation & Testing
- Use MCP validation tools to verify configurations
- Test pipeline logic and data flow
- Document pipeline purpose and usage patterns

#### Step 6: Version Control & Deployment
**REQUIRED**: After every .slp file creation or modification:
1. **Stage Changes**: `git add {pipeline_name}.slp`
2. **Commit with Details**: Include descriptive commit message explaining:
   - Purpose and functionality of the pipeline
   - Key technical components and configurations
   - Business use case and expected outcomes
3. **Push to Remote**: `git push` to ensure repository synchronization
4. **Documentation**: Update pipeline inventory and relationships in CLAUDE.md

**Example Commit Process**:
```bash
git add NewPipeline.slp
git commit -m "Add new data processing pipeline

- Implements CSV to JSON transformation workflow
- Configurable output paths and data validation
- Follows Designer-friendly formatting guidelines

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
git push
```

**Note**: This step ensures all pipeline changes are properly versioned, documented, and available to the team immediately after creation.

### 3. Snap Configuration Best Practices

**Essential Snap Categories**:

#### Transform Snaps
- **CSV Generator**: `com-snaplogic-snaps-transform-csvgenerator`
- **CSV Formatter**: `com-snaplogic-snaps-transform-csvformatter`
- **JSON Splitter**: Data parsing and transformation
- **Mapper**: Field mapping and data transformation

#### AI/ML Integration
- **AWS Bedrock**: `com-snaplogic-snaps-aws-bedrock-*`
- **Azure OpenAI**: `com-snaplogic-snaps-azure-openai-*`
- **Function Calling**: LLM tool integration capabilities

#### File & Data Operations
- **File Writer**: `com-snaplogic-snaps-binary-write`
- **File Reader**: Data ingestion from various sources
- **MongoDB Operations**: Document database integration
- **HTTP Client**: API integration and web service calls

#### Flow Control
- **Router**: Conditional data routing
- **Gate**: Flow control and synchronization
- **Copy**: Data duplication and parallel processing
- **Union**: Data stream merging

**Configuration Standards**:
- **Execution Mode**: Use "Validate & Execute" for development, "Execute only" for production
- **Error Handling**: Implement proper error behaviors (fail/continue/ignore)
- **Pipeline Parameters**: Use expressions for dynamic configuration
- **Account Management**: Proper authentication and authorization setup

### File Writer Configuration Guidelines

#### File Path Rules
**CRITICAL**: Unless specifically prompted otherwise, always write files to SLDB (SnapLogic Database) using filename only:
- **Correct**: `"dashboard.html"`, `"report.csv"`, `"data.json"`
- **Incorrect**: `"/tmp/dashboard.html"`, `"/path/to/file.csv"`
- **Purpose**: SLDB provides managed storage without path complexity
- **Exception**: Only use full paths when explicitly requested by user

#### Binary Input Requirement
**File Writer (Binary Write) Snap Requirements**:
- **Input View Type**: Must be **binary** (not document)
- **Common Error**: Connecting document output directly to File Writer
- **Solution**: Use Document to Binary conversion snap when needed

#### Document to Binary Conversion Pattern
When LLM/AI snaps output document format but File Writer needs binary:

**Pipeline Pattern**:
```
AI/LLM Snap (document) → [Mapper] → Document to Binary → File Writer (binary)
```

**Key Snap**: `com-snaplogic-snaps-transform-documenttobinary`
- **Purpose**: Converts document `content` field to binary data
- **Configuration**: Set appropriate codec (usually `NONE` for plain text)
- **Field Requirement**: Input document must have `content` field

**Mapper Configuration** (if needed to extract LLM response):
```json
{
  "content": "$output.message.content[0].text"  // Extract text from LLM response
}
```

**AWS Bedrock Specific**: Enable `simplifyResponse` for cleaner field structure

### 4. Integration Patterns

**Common Integration Scenarios**:

#### API Integration Pattern
```
JSON Generator → HTTP Client → Response Parser → Data Transform
```

#### File Processing Pattern
```
File Reader → Parser (CSV/JSON) → Transform → Writer
```

#### AI Processing Pattern
```
Data Prep → AI/ML Snap → Response Processing → Output Format
```

#### Workflow Orchestration Pattern
```
Trigger → Router → Parallel Processing → Gate → Final Output
```

## Development Guidelines

### Pipeline Naming Convention
- Use descriptive names that indicate purpose and main components
- Include version information for iterative development
- Examples: `AgentDriverAutoRFPAnalystRFIs.slp`, `TextToSpeechElevenLabs-MCPGenerated.slp`

### Documentation Requirements
1. **Pipeline Purpose**: Clear description of business use case
2. **Data Flow**: Input → Processing → Output documentation
3. **Configuration Notes**: Key parameters and customization options
4. **Integration Points**: External systems and dependencies
5. **Error Handling**: Failure scenarios and recovery strategies

### Quality Assurance
- **Schema Validation**: Always use MCP tool for snap schema verification
- **Configuration Testing**: Validate all snap settings and parameters
- **Data Flow Testing**: Ensure proper input/output view connections
- **Error Scenarios**: Test failure modes and error handling
- **Performance Optimization**: Monitor execution times and resource usage

### Version Control Best Practices
- **Commit Messages**: Descriptive messages following repository patterns
- **Change Documentation**: Detail modifications and their impact
- **Backup Strategy**: Maintain .backup files for critical pipelines
- **Collaborative Development**: Use proper branching for multi-developer work

<!-- BEGIN: SnapLogic Designer-Friendly Pipeline Guidelines -->
### SnapLogic Designer Visual Layout Guidelines

When creating pipelines for optimal SnapLogic Designer canvas rendering and visual connectivity:

#### Snap ID Format
- **Use Sequential UUID Pattern**: `11111111-1111-1111-1111-00000000000X` where X increments (0, 1, 2, etc.)
- **Purpose**: Maintains compatibility with SnapLogic Designer's ID management system
- **Example**: Generator (000000000000) → Formatter (000000000001) → Writer (000000000002)

#### Grid Positioning Strategy
- **Linear Pipelines**: Use single row with incremental x-positions
  - **Formula**: `grid(start_x + index, row_y)` 
  - **Recommended**: Start at x=4, use row=1 for clean layout
  - **Spacing**: Increment x by 1 for directly connected snaps
- **Complex Layouts**: 
  - **Branching**: Use vertical spacing (increment y) for parallel paths
  - **Convergence**: Position merge points with adequate spacing
  - **Rule**: Maintain minimum distance of 1 grid unit between connected snaps

#### Pipeline Output Views
- **Always Create**: Explicit pipeline-level output for final snap outputs
- **Naming Convention**: `"{final_snap_id}_{output_view_name}"`
- **Structure**: Include descriptive label and correct view_type
- **Purpose**: Makes data flow endpoints visible in Designer canvas

#### JSON Formatting Standards
- **Indentation**: Use 4-space indentation for readability
- **Author Field**: Set to "jarcega@snaplogic.com" for consistency
- **Structure Order**: Follow SnapLogic standard: class_id, class_version, link_map, property_map, render_map, snap_map

#### Generator Snap Content Formatting
**CRITICAL**: SnapLogic Designer requires proper newline handling in generator snap `editable_content` fields:

- **Use Actual Newlines**: Content must contain real `\n` characters, NOT escaped `\\n` sequences
- **JSON Generator**: Format JSON with proper line breaks for readability:
  ```json
  "editable_content": {
    "value": "[\n  {\n    \"field1\": \"value1\",\n    \"field2\": \"value2\"\n  }\n]"
  }
  ```
- **CSV Generator**: Format CSV with proper row separation:
  ```json
  "editable_content": {
    "value": "Header1,Header2,Header3\nValue1,Value2,Value3\nValue4,Value5,Value6"
  }
  ```

**Common Error**: Using `\\n` (escaped sequences) causes "invalid character" warnings in Designer
**Correct Approach**: Use actual newline characters for proper parsing and display
**Impact**: Ensures content displays correctly in Designer canvas and validates without errors

#### Expression Field Syntax Requirements
**CRITICAL**: SnapLogic has strict requirements for expression field syntax that can prevent pipeline recognition:

- **String Literals Must Be Quoted**: All string literals in expressions must be wrapped in double quotes
- **String Concatenation**: Use proper SnapLogic concatenation with quoted string segments
- **Date Functions**: Use SnapLogic-compatible date methods, not JavaScript equivalents

**Examples of Correct Expression Syntax**:

**SOQL Query with Dynamic Parameters**:
```json
"soqlQuery": {
  "expression": true,
  "value": "\"SELECT Id, Name FROM Opportunity WHERE CloseDate >= LAST_N_DAYS:\" + $days_back + \" AND Region__c LIKE 'EMEA_%'\""
}
```

**Dynamic Filename Generation**:
```json
"filename": {
  "expression": true,
  "value": "\"/tmp/dashboard_\" + Date.now().toLocaleDateString().replace(/\\//g,'') + \".html\""
}
```

**Common Errors That Break Pipeline Recognition**:
- ❌ `"SELECT ... LAST_N_DAYS:" + $days_back + " AND ..."` (unquoted strings)
- ❌ `Date.now().toISOString().substring(0,10)` (JavaScript methods)
- ❌ Missing escape characters in regex patterns

**Correct Approaches**:
- ✅ `"\"SELECT ... LAST_N_DAYS:\" + $days_back + \" AND ...\""` (quoted string segments)
- ✅ `Date.now().toLocaleDateString().replace(/\\//g,'')` (SnapLogic date methods)
- ✅ Properly escaped backslashes in regex: `replace(/\\//g,'')`

**Impact**: Incorrect expression syntax prevents SnapLogic from recognizing .slp files as valid pipelines, treating them as generic files instead. This issue was discovered when a pipeline with invalid expressions appeared as a file rather than a pipeline in SnapLogic Designer.

### Pipeline Parameter and Data Reference Guidelines

#### Pipeline Parameter References
When referencing pipeline parameters in expressions, use underscore prefix syntax:
- **Correct**: `_parameterName` 
- **Incorrect**: `$parameterName`

**Example**:
```json
"soqlQuery": {
  "expression": true,
  "value": "\"SELECT Id, Name FROM Opportunity WHERE CloseDate >= LAST_N_DAYS:\" + _days_back + \" AND Region__c LIKE 'EMEA_%'\""
}
```

#### Document Grouping for Aggregate Processing
When processing multiple documents with AI/LLM snaps, use the Group By N snap to collect documents:
- **Group By N Configuration**: Set `groupSize: 0` to collect all documents
- **Target Field**: Set `targetField` to desired array name (e.g., "opportunities")
- **Purpose**: Enables AI processing of all documents at once instead of individually

**Pattern**:
```
Data Source → Mapper → Filter → Group By N → Prompt → AI/LLM → Output
```

#### Prompt Template Data References
In prompt snaps, use mustache template syntax to reference grouped data:
- **Correct**: `{{opportunities}}` or `{{$opportunities}}`
- **Context**: References the array created by Group By N snap
- **Usage**: Allows AI to process entire dataset for comprehensive analysis

**Example Prompt Content**:
```
Analyze the following opportunity data: {{$opportunities}}
Generate insights and recommendations based on all opportunities.
```

### AWS Bedrock Snap Configuration

#### System Prompt Configuration
For hardcoded system prompts in AWS Bedrock Converse API snap:
- **Expression Mode**: Disable expression for static text
- **Alternative**: Wrap text in quotes if using expression mode
- **Purpose**: Prevents unnecessary expression evaluation for static content

**Example - Non-Expression Mode**:
```json
"systemPrompt": {
  "expression": false,
  "value": "You are an expert solutions engineer analyst."
}
```

**Example - Expression Mode with Quotes**:
```json
"systemPrompt": {
  "expression": true,
  "value": "\"You are an expert solutions engineer analyst.\""
}
```

#### AWS Bedrock Prompt Generator to Converse API Data Flow
**CRITICAL**: The Prompt Generator's `advancedMode` setting determines the output format and required Converse API configuration.

### Configuration Pattern 1: Simple Mode (advancedMode = false)
**Prompt Generator Output**: Simple text string in `$prompt` field
**Converse API Configuration**:
- **useMessages**: Set to `false` (unchecked)
- **prompt**: Reference Prompt Generator output via `$prompt`

**Pipeline Configuration Example**:
```json
// Prompt Generator
{
  "advancedMode": false,
  "content": "$input_data"
}

// Converse API  
{
  "useMessages": false,
  "prompt": "$prompt",
  "model": "_bedrock_model"
}
```

### Configuration Pattern 2: Advanced Mode (advancedMode = true)
**Prompt Generator Output**: Complete message array structure in `$messages` field
**Converse API Configuration**:
- **useMessages**: Set to `true` (checked)
- **messages**: Reference Prompt Generator output via `$messages`

**Pipeline Configuration Example**:
```json
// Prompt Generator
{
  "advancedMode": true,
  "role": "USER",
  "content": "$input_data"
}

// Converse API  
{
  "useMessages": true,
  "messages": "$messages",
  "model": "_bedrock_model"
}
```

### Field Reference Rules
**CRITICAL FIELD REFERENCES**:
- **Simple Mode Output**: `$prompt` (contains the generated prompt text)
- **Advanced Mode Output**: `$messages` (contains the complete message array)
- **Never Use**: `$` alone - this references the entire document, not the specific prompt output

### Key Technical Details
1. **Field Mapping**: 
   - Simple Mode: Prompt Generator `$prompt` → Converse API `prompt` field
   - Advanced Mode: Prompt Generator `$messages` → Converse API `messages` field
2. **Mode Matching**: Prompt Generator `advancedMode` must match Converse API `useMessages` setting
3. **Automatic Translation**: SnapLogic automatically formats advanced mode output for LLM compatibility
4. **Expression References**: Always use the specific field name (`$prompt` or `$messages`), not generic `$`

#### Connection Optimization
- **Short Paths**: Position snaps to minimize connection line length
- **Avoid Crossovers**: Layout snaps to prevent connection lines from crossing
- **Visual Flow**: Arrange left-to-right for data flow clarity
- **Grouping**: Keep related snaps visually grouped when possible

#### Layout Examples
```
Linear (3-snap): Generator(4,1) → Formatter(5,1) → Writer(6,1)
Branch (4-snap): Source(3,1) → Router(4,1) → {PathA(5,0), PathB(5,2)}
Merge (4-snap): {SourceA(3,0), SourceB(3,2)} → Union(4,1) → Writer(5,1)
```

**Note**: These guidelines optimize for visual clarity and Designer compatibility while maintaining functional pipeline behavior.
<!-- END: SnapLogic Designer-Friendly Pipeline Guidelines -->

## Troubleshooting & Support

### Common Issues
1. **Schema Mismatches**: Use MCP tool to get current snap schemas
2. **Configuration Errors**: Reference SnapLogic documentation for valid settings
3. **Integration Failures**: Check account configurations and network connectivity
4. **Performance Issues**: Review pipeline design and optimize data flow

### Debug Strategies
1. **Enable Debug Mode**: Use "Validate & Execute" with detailed logging
2. **Incremental Testing**: Test individual snaps before full pipeline execution
3. **Data Inspection**: Use preview capabilities to examine data at each stage
4. **Error Analysis**: Review error messages and stack traces for root cause

### Resources for Additional Support
- **SnapLogic Community**: Forums and user discussions
- **Official Documentation**: Comprehensive snap references and tutorials  
- **Training Materials**: SnapLogic University courses and certifications
- **Professional Services**: Expert consultation for complex implementations

## Conclusion

This repository represents a comprehensive SnapLogic integration environment with sophisticated AI-powered workflows, data processing capabilities, and business automation solutions. The combination of MCP tooling, extensive documentation resources, and proven pipeline patterns provides a robust foundation for developing, maintaining, and extending SnapLogic integrations.

When working with these assets, always prioritize schema validation, thorough testing, and comprehensive documentation to ensure reliable, maintainable, and scalable integration solutions.