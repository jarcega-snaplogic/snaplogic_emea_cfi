"""
SnapLogic API Client for CI/CD operations.
Handles authentication and communication with SnapLogic REST APIs.
"""

import base64
import json
import os
import requests
from typing import Dict, List, Optional, Any
import time
from pathlib import Path


class SnapLogicAPIClient:
    """Client for interacting with SnapLogic Public APIs."""
    
    def __init__(self, config_path: str = ".snaplogic-config.json"):
        """Initialize API client with configuration."""
        self.config = self._load_config(config_path)
        self.session = requests.Session()
        self._setup_auth()
        self.base_url = f"{self.config['api']['base_url']}/api/1"
        
    def _load_config(self, config_path: str) -> dict:
        """Load configuration from JSON file."""
        config_file = Path(config_path)
        if not config_file.exists():
            # Try parent directory
            config_file = Path("..") / config_path
        if not config_file.exists():
            raise FileNotFoundError(f"Config file not found: {config_path}")
            
        with open(config_file, 'r') as f:
            return json.load(f)
    
    def _setup_auth(self):
        """Setup authentication headers."""
        username = self.config['credentials']['username']
        password = self.config['credentials']['password']
        
        # Create Basic Auth header
        credentials = f"{username}:{password}"
        encoded_credentials = base64.b64encode(credentials.encode()).decode()
        
        self.session.headers.update({
            'Authorization': f'Basic {encoded_credentials}',
            'Content-Type': 'application/json'
        })
    
    def _get_project_path(self) -> str:
        """Construct the project path from config."""
        org = self.config['api']['org']
        space = self.config['api']['project_space']
        return f"{org}/{space}"
    
    def import_pipeline(self, pipeline_path: str, **options) -> Dict[str, Any]:
        """
        Import a pipeline file to SnapLogic.
        
        Args:
            pipeline_path: Path to local .slp file
            options: force, dry_run, backup options
            
        Returns:
            Result dictionary with status and message
        """
        if not os.path.exists(pipeline_path):
            return {'status': 'error', 'message': f'Pipeline file not found: {pipeline_path}'}
        
        pipeline_name = os.path.basename(pipeline_path).replace('.slp', '')
        
        # Read pipeline content
        with open(pipeline_path, 'r') as f:
            pipeline_content = json.load(f)
        
        if options.get('dry_run', False):
            return {
                'status': 'success',
                'message': f'[DRY RUN] Would import pipeline: {pipeline_name}',
                'pipeline': pipeline_name
            }
        
        # Backup existing pipeline if requested
        if options.get('backup', True):
            self._backup_pipeline(pipeline_name)
        
        # Import endpoint
        url = f"{self.base_url}/rest/public/project/import/{self._get_project_path()}/{pipeline_name}"
        
        # Prepare multipart form data
        files = {
            'file': (f'{pipeline_name}.slp', json.dumps(pipeline_content), 'application/json')
        }
        
        # Update headers for multipart
        headers = self.session.headers.copy()
        headers.pop('Content-Type', None)  # Let requests set the boundary
        
        params = {
            'duplicate_check': not options.get('force', False)
        }
        
        try:
            response = self.session.post(url, files=files, headers=headers, params=params)
            response.raise_for_status()
            
            return {
                'status': 'success',
                'message': f'Successfully imported pipeline: {pipeline_name}',
                'pipeline': pipeline_name,
                'response': response.json() if response.text else {}
            }
        except requests.exceptions.RequestException as e:
            return {
                'status': 'error',
                'message': f'Failed to import pipeline: {str(e)}',
                'pipeline': pipeline_name
            }
    
    def export_pipeline(self, pipeline_name: Optional[str] = None, **options) -> Dict[str, Any]:
        """
        Export pipeline(s) from SnapLogic to local files.
        
        Args:
            pipeline_name: Specific pipeline or None for all
            options: overwrite option
            
        Returns:
            Result dictionary with exported files list
        """
        if pipeline_name:
            # Export single pipeline
            url = f"{self.base_url}/rest/public/project/export/{self._get_project_path()}/{pipeline_name}"
        else:
            # Export entire project
            url = f"{self.base_url}/rest/public/project/export/{self._get_project_path()}"
        
        try:
            response = self.session.get(url)
            response.raise_for_status()
            
            exported_files = []
            
            if pipeline_name:
                # Single pipeline export
                pipeline_data = response.json()
                file_path = f"{pipeline_name}.slp"
                
                if os.path.exists(file_path) and not options.get('overwrite', False):
                    return {
                        'status': 'error',
                        'message': f'Pipeline file already exists: {file_path}. Use overwrite=True to replace.'
                    }
                
                with open(file_path, 'w') as f:
                    json.dump(pipeline_data, f, indent=2)
                
                exported_files.append(file_path)
            else:
                # Multiple pipelines - response should contain project data
                project_data = response.json()
                
                # Extract pipelines from project data
                if 'pipelines' in project_data:
                    for pipeline in project_data['pipelines']:
                        file_path = f"{pipeline['name']}.slp"
                        
                        if os.path.exists(file_path) and not options.get('overwrite', False):
                            continue
                        
                        with open(file_path, 'w') as f:
                            json.dump(pipeline['content'], f, indent=2)
                        
                        exported_files.append(file_path)
            
            return {
                'status': 'success',
                'message': f'Successfully exported {len(exported_files)} pipeline(s)',
                'files': exported_files
            }
            
        except requests.exceptions.RequestException as e:
            return {
                'status': 'error',
                'message': f'Failed to export pipeline(s): {str(e)}'
            }
    
    def list_project_assets(self, asset_type: str = "pipeline") -> List[str]:
        """
        List all assets in the SnapLogic project.
        
        Args:
            asset_type: Type of assets to list (pipeline, account, file, etc.)
            
        Returns:
            List of asset names
        """
        url = f"{self.base_url}/rest/public/assets/{self._get_project_path()}"
        
        params = {
            'asset_type': asset_type
        }
        
        try:
            response = self.session.get(url, params=params)
            response.raise_for_status()
            
            assets = response.json()
            return [asset['name'] for asset in assets.get('entries', [])]
            
        except requests.exceptions.RequestException as e:
            print(f"Error listing assets: {str(e)}")
            return []
    
    def pull_from_git(self) -> Dict[str, Any]:
        """
        Pull latest changes from Git repository configured in SnapLogic.
        
        Returns:
            Result dictionary with pull status
        """
        url = f"{self.base_url}/rest/public/project/pull/{self._get_project_path()}"
        
        try:
            response = self.session.post(url)
            response.raise_for_status()
            
            return {
                'status': 'success',
                'message': 'Successfully pulled from Git repository',
                'response': response.json() if response.text else {}
            }
            
        except requests.exceptions.RequestException as e:
            return {
                'status': 'error',
                'message': f'Failed to pull from Git: {str(e)}'
            }
    
    def _backup_pipeline(self, pipeline_name: str):
        """Create a backup of existing pipeline before overwriting."""
        backup_dir = Path(".snaplogic-backups")
        backup_dir.mkdir(exist_ok=True)
        
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        backup_path = backup_dir / f"{pipeline_name}_{timestamp}.slp"
        
        # Export current version as backup
        export_result = self.export_pipeline(pipeline_name)
        if export_result['status'] == 'success' and export_result['files']:
            original_file = export_result['files'][0]
            if os.path.exists(original_file):
                os.rename(original_file, str(backup_path))
                print(f"Backup created: {backup_path}")