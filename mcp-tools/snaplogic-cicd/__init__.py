"""
SnapLogic CI/CD MCP Tool

Provides seamless integration between local Git repository and SnapLogic platform.
Enables push/pull operations, synchronization status checks, and pipeline validation.
"""

from .api_client import SnapLogicAPIClient
from .git_integration import GitIntegration
from .validators import PipelineValidator

__version__ = "1.0.0"
__author__ = "jarcega@snaplogic.com"

# MCP Tool functions that will be exposed
def push_to_snaplogic(pipeline_path, **options):
    """
    Push a local pipeline file to SnapLogic.
    
    Args:
        pipeline_path: Path to the .slp file
        options: Additional options (force, dry_run, backup)
    
    Returns:
        dict: Result with status and message
    """
    client = SnapLogicAPIClient()
    validator = PipelineValidator()
    
    # Validate pipeline first
    if options.get('validate', True):
        validation_result = validator.validate(pipeline_path)
        if not validation_result['valid']:
            return {
                'status': 'error',
                'message': f"Validation failed: {validation_result['errors']}"
            }
    
    # Push to SnapLogic
    return client.import_pipeline(pipeline_path, **options)


def pull_from_snaplogic(pipeline_name=None, **options):
    """
    Pull pipeline(s) from SnapLogic to local repository.
    
    Args:
        pipeline_name: Specific pipeline name or None for all
        options: Additional options (overwrite, commit_message)
    
    Returns:
        dict: Result with status and list of pulled pipelines
    """
    client = SnapLogicAPIClient()
    git = GitIntegration()
    
    # Export from SnapLogic
    result = client.export_pipeline(pipeline_name, **options)
    
    # Auto-commit if configured
    if result['status'] == 'success' and options.get('commit', True):
        git.commit_changes(
            result['files'],
            options.get('commit_message', f"Pull pipeline(s) from SnapLogic")
        )
    
    return result


def sync_status(**options):
    """
    Check synchronization status between local and SnapLogic.
    
    Returns:
        dict: Status information with differences
    """
    client = SnapLogicAPIClient()
    git = GitIntegration()
    
    local_pipelines = git.list_pipeline_files()
    remote_pipelines = client.list_project_assets()
    
    return {
        'status': 'success',
        'local_only': list(set(local_pipelines) - set(remote_pipelines)),
        'remote_only': list(set(remote_pipelines) - set(local_pipelines)),
        'both': list(set(local_pipelines) & set(remote_pipelines)),
        'total_local': len(local_pipelines),
        'total_remote': len(remote_pipelines)
    }


def validate_pipeline(pipeline_path, **options):
    """
    Validate a pipeline file before pushing to SnapLogic.
    
    Args:
        pipeline_path: Path to the .slp file
        
    Returns:
        dict: Validation result with details
    """
    validator = PipelineValidator()
    return validator.validate(pipeline_path, detailed=True)


def list_project_assets(**options):
    """
    List all assets in the SnapLogic project.
    
    Returns:
        dict: List of project assets with metadata
    """
    client = SnapLogicAPIClient()
    return client.list_project_assets(**options)


# MCP Tool metadata
MCP_TOOL_DEFINITION = {
    "name": "snaplogic-cicd",
    "description": "SnapLogic CI/CD integration for pipeline synchronization",
    "version": __version__,
    "functions": {
        "push_to_snaplogic": {
            "description": "Push local pipeline to SnapLogic",
            "parameters": {
                "pipeline_path": {"type": "string", "required": True},
                "force": {"type": "boolean", "default": False},
                "dry_run": {"type": "boolean", "default": False},
                "backup": {"type": "boolean", "default": True},
                "validate": {"type": "boolean", "default": True}
            }
        },
        "pull_from_snaplogic": {
            "description": "Pull pipeline from SnapLogic to local",
            "parameters": {
                "pipeline_name": {"type": "string", "required": False},
                "overwrite": {"type": "boolean", "default": False},
                "commit": {"type": "boolean", "default": True},
                "commit_message": {"type": "string", "required": False}
            }
        },
        "sync_status": {
            "description": "Check sync status between local and SnapLogic",
            "parameters": {}
        },
        "validate_pipeline": {
            "description": "Validate pipeline file",
            "parameters": {
                "pipeline_path": {"type": "string", "required": True}
            }
        },
        "list_project_assets": {
            "description": "List all project assets in SnapLogic",
            "parameters": {
                "asset_type": {"type": "string", "default": "pipeline"}
            }
        }
    }
}