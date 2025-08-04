# SnapLogic Pipeline Development Rules

A comprehensive reference guide for SnapLogic pipeline development best practices, requirements, and critical configuration rules.

## Core Development Rules

| Category | Rule | Criticality | Description | Example |
|----------|------|-------------|-------------|---------|
| **Schema Validation** | Always use MCP Schema Tool | CRITICAL | Validate all snap configurations before deployment | `mcp__snaplogic-schema__get_snap_schema` |
| **Property Order** | Follow MCP template structure | HIGH | Use exact property order from MCP-generated templates | `view_serial → settings → input → output → error → info` |
| **File Paths** | Write to SLDB by default | CRITICAL | Use filename only unless explicitly requested otherwise | `"filename.csv"` not `"/tmp/filename.csv"` |
| **Author Field** | Set consistent author | MEDIUM | Use "jarcega@snaplogic.com" for consistency | Required in all pipeline info sections |
| **UUID Pattern** | Sequential UUID format | HIGH | Use incremental pattern for Designer compatibility | `11111111-1111-1111-1111-00000000000X` |

## Snap Configuration Requirements

| Snap Type | Class ID | Minimum Views | Critical Properties | Notes |
|-----------|----------|---------------|-------------------|-------|
| **Copy** | `com-snaplogic-snaps-flow-copy` | 2 outputs (output0, output1) | render_map output positioning | Must have `output1: {dx_int: 0, dy_int: 1, rot_int: 0}` |
| **Router** | `com-snaplogic-snaps-flow-router` | Multiple outputs | Routing conditions | Based on conditional logic |
| **Union** | `com-snaplogic-snaps-flow-union` | 2+ inputs | Merge configuration | Combines multiple streams |
| **Join** | `com-snaplogic-snaps-transform-join` | 2 inputs (left, right) | Join conditions | Relational operations |
| **Diff** | `com-snaplogic-snaps-transform-diff` | Multiple outputs | Comparison results | Separate matching/added/removed |
| **Mapper** | `com-snaplogic-snaps-transform-datatransform` | 1 input, 1 output | `nullSafeAccess`, `passThrough`, `mappingRoot` | Always set `nullSafeAccess: true` |
| **File Writer** | `com-snaplogic-snaps-binary-write` | Binary input required | Complete settings properties | 20+ required configuration properties |

## Expression Syntax Rules

| Rule Type | Requirement | Criticality | Correct Example | Incorrect Example |
|-----------|-------------|-------------|-----------------|-------------------|
| **String Literals** | Must be quoted | CRITICAL | `"\"SELECT * FROM Table\""` | `"SELECT * FROM Table"` |
| **Parameter References** | Use underscore prefix | HIGH | `_parameterName` | `$parameterName` |
| **String Concatenation** | Quote all segments | HIGH | `"\"prefix\" + _param + \"suffix\""` | `"prefix" + _param + "suffix"` |
| **Date Functions** | Use SnapLogic methods | MEDIUM | `Date.now().toLocaleDateString()` | `Date.now().toISOString()` |
| **Regex Escaping** | Proper backslash escape | MEDIUM | `replace(/\\//g,'')` | `replace(/\//g,'')` |

## File Writer Snap Patterns

| Pattern | Binary Input | Document to Binary | Configuration |
|---------|--------------|-------------------|---------------|
| **Direct Binary** | ✅ Required | ❌ Not needed | AI/LLM → File Writer (fails) |
| **Document Conversion** | ✅ Required | ✅ Required | AI/LLM → Doc to Binary → File Writer |
| **CSV Processing** | ✅ Required | ✅ Via CSV Formatter | Generator → CSV Formatter → File Writer |

**Critical Rule**: File Writer (Binary Write) snaps require binary input. Document outputs must be converted using Document to Binary snap.

## AWS Bedrock Configuration Patterns

| Prompt Mode | Prompt Generator | Converse API | Field Reference |
|-------------|------------------|--------------|-----------------|
| **Simple** | `advancedMode: false` | `useMessages: false` | `$prompt` |
| **Advanced** | `advancedMode: true` | `useMessages: true` | `$messages` |

**Critical Rule**: Prompt Generator `advancedMode` must match Converse API `useMessages` setting.

## Render Map Configuration Rules

| Snap Type | Required Configuration | Visual Impact | Critical Fields |
|-----------|----------------------|---------------|-----------------|
| **Copy Snap** | Output positioning for output1 | Shows both outputs in GUI | `dx_int: 0, dy_int: 1, rot_int: 0` |
| **Router Snap** | Multiple output positioning | Proper branch visualization | Conditional output placement |
| **Single Output** | Empty output object | Standard display | `"output": {}` |
| **Grid Position** | X/Y coordinates | Canvas layout | `grid_x_int`, `grid_y_int` |

**Critical Rule**: Multi-output snaps need render_map output configuration or only first output appears in Designer GUI.

## Generator Snap Content Formatting

| Content Type | Newline Requirement | Correct Format | Incorrect Format |
|--------------|-------------------|----------------|------------------|
| **JSON Generator** | Real newlines | `"[\n  {\n    \"field\": \"value\"\n  }\n]"` | `"[\\n  {\\n    \"field\": \"value\"\\n  }\\n]"` |
| **CSV Generator** | Real newlines | `"Header1,Header2\nValue1,Value2"` | `"Header1,Header2\\nValue1,Value2"` |

**Critical Rule**: Use actual `\n` characters, not escaped `\\n` sequences in generator content.

## Pipeline Parameter and Data Reference

| Reference Type | Syntax | Usage Context | Example |
|----------------|--------|---------------|---------|
| **Pipeline Parameters** | `_parameterName` | Expression fields | `_days_back` |
| **Document Fields** | `$fieldName` | Data references | `$prompt`, `$messages` |
| **Group By N Output** | `{{arrayName}}` | Prompt templates | `{{$opportunities}}` |
| **Current Document** | `$` | Generic reference | Avoid - use specific fields |

## Data Grouping and Flow Patterns

| Pattern | Snap Sequence | Purpose | Critical Notes |
|---------|---------------|---------|----------------|
| **Individual Processing** | Source → Transform → Output | Process each document | Standard pattern |
| **Aggregate Processing** | Source → Group By N → AI/LLM | Process all together | Set `groupSize: 0` |
| **Data Splitting** | Source → Copy → Multiple paths | Parallel processing | Requires render_map config |
| **Data Merging** | Multiple → Union → Single | Combine streams | Minimum 2 inputs |

**Critical Rule**: When in doubt about data grouping patterns, ask user for guidance - these patterns require experience to implement correctly.

## Common Error Prevention

| Error Type | Symptoms | Root Cause | Solution |
|------------|----------|------------|----------|
| **Pipeline Not Recognized** | .slp treated as generic file | Invalid expression syntax | Quote all string literals in expressions |
| **Copy Snap Single Output** | Only output0 visible in GUI | Missing render_map output config | Add output1 positioning in render_map |
| **File Writer Failure** | Binary input required error | Document → File Writer directly | Insert Document to Binary conversion |
| **Schema Validation Failure** | Missing required properties | Incomplete snap configuration | Use MCP tool for complete templates |
| **Expression Evaluation Error** | Unquoted string literals | Invalid expression syntax | Wrap all strings in double quotes |
| **CSV Generator → Parser Error** | Pipeline rejected by Designer | Data type mismatch | CSV Generator outputs documents, not binary - use Generator → Mapper directly |

## Version Control Best Practices

| Action | Requirement | Criticality | Implementation |
|--------|-------------|-------------|----------------|
| **Every .slp Change** | Commit and push | REQUIRED | `git add`, `git commit`, `git push` |
| **Commit Messages** | Descriptive with technical details | HIGH | Include purpose, components, use case |
| **Pipeline Documentation** | Update inventory in CLAUDE.md | MEDIUM | Track relationships and dependencies |
| **Backup Strategy** | Maintain .backup files | MEDIUM | Keep critical pipeline versions |

## Troubleshooting Quick Reference

| Issue | Check First | MCP Command | Fix Pattern |
|-------|-------------|-------------|-------------|
| **Schema Errors** | Required properties | `get_snap_schema` | Add missing properties |
| **GUI Display Issues** | Render map configuration | Visual inspection | Add output positioning |
| **Expression Failures** | String quoting | Syntax validation | Quote all literals |
| **File Writer Errors** | Input view type | Binary requirement | Add Doc to Binary |
| **Multi-view Problems** | Minimum view counts | Schema validation | Add required views |

---

## Usage Guidelines

1. **Reference Priority**: Use this table for quick lookups during development
2. **Schema Validation**: Always validate configurations using MCP tool
3. **Pattern Recognition**: Identify common patterns before implementation  
4. **Error Prevention**: Check common error scenarios before deployment
5. **User Consultation**: Ask user for guidance on complex data flow patterns

**Note**: This reference complements the detailed explanations in CLAUDE.md and should be used together for comprehensive SnapLogic development guidance.