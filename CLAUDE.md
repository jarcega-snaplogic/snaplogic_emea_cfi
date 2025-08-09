# CLAUDE.md - SnapLogic Development Guide

Agent-driven SnapLogic development with validation-first workflow. This guide coordinates specialized agents and modular rule loading for production-grade pipeline development.

## 🎯 Core Development Principles

- **Production-Grade Always**: Build for enterprise reliability, not prototypes
- **Agent-Assisted Development**: Use specialized agents for complex SnapLogic tasks
- **Validation-First Workflow**: Git hooks prevent invalid pipelines from reaching repository
- **Modular Knowledge Loading**: Load specific rule files contextually to reduce cognitive load
- **Pattern-Based Architecture**: Follow proven patterns from vibe-coding-snaplogic examples

## 🤖 Custom Agents System

This project uses **Claude Code's custom agent feature** with two specialized agents available in `.claude/agents/`:

### snaplogic-pipeline-developer
**True Custom Agent**: Automatically triggered for SnapLogic pipeline development tasks
- **Purpose**: Create, modify, and work with SnapLogic pipelines (.slp files)
- **Triggers**: Pipeline creation, field transformations, complex multi-snap workflows
- **Knowledge**: Comprehensive rule file access + MCP tool integration
- **Capabilities**: Production-grade pipeline development following all standards

### snaplogic-validation-troubleshooter  
**True Custom Agent**: Automatically triggered for validation issues and troubleshooting
- **Purpose**: Debug validation failures and systematic remediation
- **Triggers**: Validation errors, JSON syntax issues, UUID mismatches, Designer problems
- **Knowledge**: Complete validation rule access + MCP schema tools
- **Capabilities**: Systematic debugging with step-by-step fixes

### Agent Usage
- **Automatic Selection**: Claude Code automatically chooses the right agent based on your request
- **Manual Invocation**: "Use the snaplogic-pipeline-developer agent to create this pipeline"
- **View Available**: Run `/agents` to see all custom agents
- **Modification**: Edit agent files in `.claude/agents/` as needed

## 📚 Modular Rule Loading

### Context-Sensitive Loading
Load specific rule files based on task context:

- **Pipeline Creation/Modification**: Load snaplogic-pipeline-rules.md + snaplogic-snap-schemas.md
- **Field Transformations/Business Logic**: Load field-transformation-rules.md
- **Validation Issues/Troubleshooting**: Load validation-checklist.md
- **Performance/Architecture Decisions**: Load pipeline patterns from vibe-coding-snaplogic docs

### Reference Resources
- **Validation Tools**: vibe-coding-snaplogic/validation/ (hooks, scripts, rules)
- **Working Examples**: vibe-coding-snaplogic/examples/ (2-snap, 3-snap, Group By N patterns)
- **Comprehensive Documentation**: vibe-coding-snaplogic/docs/ (patterns, best practices)
- **Rule Backup**: CLAUDE.md.backup (full original documentation)

## 🔄 Development Workflow

### Development Workflow
1. **Planning**: Define use case, select pattern, plan data flow
2. **Automatic Agent Selection**: Claude Code automatically selects the appropriate custom agent
3. **Knowledge Loading**: Agents automatically load relevant rule files and use MCP tools
4. **Implementation**: Build using validated patterns and comprehensive rule compliance
5. **Validation**: Git hooks automatically validate on commit
6. **Testing**: Verify with sample data and validation tools
7. **Documentation**: Update notes and commit with descriptive messages

### When Custom Agents Are Used
- **snaplogic-pipeline-developer**: Automatically triggered for pipeline creation, modification, multi-snap workflows, field transformations
- **snaplogic-validation-troubleshooter**: Automatically triggered for validation errors, JSON syntax issues, UUID problems, Designer issues
- **Manual Tasks**: Simple single-value changes, documentation updates, non-pipeline operations

## 🛠️ Environment Configuration

### SnapLogic Platform Details
- **Environment**: SnapLogic EMEA Cloud (ConnectFasterInc)
- **Base URL**: https://emea.snaplogic.com
- **Project**: snapLogic4snapLogic/tryGit
- **Author**: jarcega@snaplogic.com

### Environment Variable Setup (REQUIRED)

**✅ COMPLETED**: SnapLogic environment variables have been added to your shell profile (`~/.bashrc`). 

**To activate the environment variables:**

1. **Reload your shell profile:**
   ```bash
   source ~/.bashrc
   ```

2. **Or restart your terminal** to automatically load the new variables

3. **Restart Claude Code** to ensure MCP tools pick up the environment variables

**Verify environment variables are loaded:**
```bash
echo "Username: $SNAPLOGIC_USERNAME"
echo "Schema URL: $SNAPLOGIC_SCHEMA_BASE_URL" 
echo "Project Space: $SNAPLOGIC_PROJECT_SPACE"
```

**Expected output:**
```
Username: jarcega@snaplogic.com
Schema URL: https://emea.snaplogic.com
Project Space: tryGit
```

**⚠️ Troubleshooting:**
- If variables show as empty, restart your terminal and Claude Code
- MCP tools will fail to connect if environment variables are not set
- Variables are automatically loaded in new terminal sessions

### Validation Infrastructure
- **Git Hooks**: Automatic .slp validation on commit (<100ms)
- **Validation Scripts**: Comprehensive analysis tools in vibe-coding-snaplogic/validation/
- **Examples Repository**: Production-ready templates at github.com/jarcega-snaplogic/vibe-coding-snaplogic
- **Emergency Override**: `git commit --no-verify` (document usage)

### File Writer Configuration (CRITICAL)
- **Default Target**: Write to SLDB using filename only ("report.csv")
- **Binary Input Required**: File Writer needs binary data, use Document to Binary converter
- **Pipeline Chaining**: End with Mapper output, not File Writer, for clean data flow

## 📋 Quick Reference Commands

### Validation
```bash
# Quick JSON check
python3 -m json.tool pipeline.slp

# Comprehensive validation  
./vibe-coding-snaplogic/validation/validate-slp.sh pipeline.slp

# Install validation hooks
./vibe-coding-snaplogic/validation/install-hooks.sh
```

### Common Patterns
- **2-Snap**: Pipeline Execute → Mapper (pipeline chaining)
- **3-Snap**: File Reader → CSV Parser → Mapper (file processing)  
- **Group By N**: Pipeline Execute → Group By N (batch processing)

### Expression Syntax (CRITICAL)  
- **String Literals**: Always quote (`"\"string literal\""`)
- **Pipeline Parameters**: Use underscore prefix (`_parameterName`)
- **Field References**: Use dollar prefix (`$fieldName`)

## 🎯 Success Metrics

- **Zero Invalid Commits**: Git hooks prevent structural issues
- **Faster Development**: Agents handle complex tasks systematically  
- **Consistent Quality**: Follow validated patterns and examples
- **Reduced Debugging**: Proactive validation prevents runtime issues
- **Knowledge Scaling**: Modular rules enable focused expertise

## 🚨 Emergency Procedures

### Validation Hook Issues
- **Hook Not Running**: `./vibe-coding-snaplogic/validation/install-hooks.sh`
- **False Positive**: Compare with working examples, report issue
- **Emergency Commit**: `git commit --no-verify` (document reason)

### Custom Agent Issues  
- **Agent Not Triggering**: Manually invoke with "Use the snaplogic-pipeline-developer agent to..."
- **View Available Agents**: Run `/agents` to see all custom agents
- **Agent Modification**: Edit agent files in `.claude/agents/` if needed
- **Knowledge Loading**: Reference rule files directly if agent unavailable
- **Fallback**: Use CLAUDE.md.backup for complete original documentation

---

**🎉 Ready for Production-Grade SnapLogic Development**

*Agents handle complexity. Validation ensures quality. Patterns enable speed.*

**Next Steps**: Try creating a pipeline - the agents will guide you through the process automatically.
