"""
Pipeline validation utilities for SnapLogic CI/CD operations.
Provides comprehensive validation of pipeline files before deployment.
"""

import json
import os
from pathlib import Path
from typing import Dict, List, Any, Tuple
import re


class PipelineValidator:
    """Validator for SnapLogic pipeline files."""
    
    def __init__(self):
        """Initialize validator with rules."""
        self.validation_rules = self._load_validation_rules()
    
    def _load_validation_rules(self) -> Dict[str, Any]:
        """Load validation rules from configuration."""
        # Basic validation rules - can be extended
        return {
            'required_fields': [
                'class_id',
                'class_version',
                'property_map',
                'snap_map',
                'link_map',
                'render_map'
            ],
            'uuid_pattern': r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$',
            'sequential_uuid_pattern': r'^11111111-1111-1111-1111-[0-9]{12}$',
            'max_file_size': 10 * 1024 * 1024,  # 10MB
            'required_pipeline_class': 'com-snaplogic-pipeline'
        }
    
    def validate(self, pipeline_path: str, detailed: bool = False) -> Dict[str, Any]:
        """
        Validate a pipeline file.
        
        Args:
            pipeline_path: Path to the .slp file
            detailed: Whether to return detailed validation information
            
        Returns:
            Validation result dictionary
        """
        result = {
            'valid': True,
            'errors': [],
            'warnings': [],
            'file': pipeline_path
        }
        
        # Check file existence
        if not os.path.exists(pipeline_path):
            result['valid'] = False
            result['errors'].append(f"Pipeline file not found: {pipeline_path}")
            return result
        
        # Check file size
        file_size = os.path.getsize(pipeline_path)
        if file_size > self.validation_rules['max_file_size']:
            result['warnings'].append(f"Large file size: {file_size} bytes")
        
        # Parse JSON
        try:
            with open(pipeline_path, 'r') as f:
                pipeline_data = json.load(f)
        except json.JSONDecodeError as e:
            result['valid'] = False
            result['errors'].append(f"Invalid JSON syntax: {str(e)}")
            return result
        
        # Validate pipeline structure
        structure_errors = self._validate_structure(pipeline_data)
        result['errors'].extend(structure_errors)
        
        # Validate UUIDs
        uuid_errors = self._validate_uuids(pipeline_data)
        result['errors'].extend(uuid_errors)
        
        # Validate snap configurations
        snap_errors = self._validate_snaps(pipeline_data)
        result['errors'].extend(snap_errors)
        
        # Validate links
        link_errors = self._validate_links(pipeline_data)
        result['errors'].extend(link_errors)
        
        # Check multi-input snaps for view_map
        view_map_warnings = self._check_view_maps(pipeline_data)
        result['warnings'].extend(view_map_warnings)
        
        # Set overall validity
        result['valid'] = len(result['errors']) == 0
        
        if detailed:
            result['details'] = self._get_detailed_info(pipeline_data)
        
        return result
    
    def _validate_structure(self, pipeline_data: Dict[str, Any]) -> List[str]:
        """Validate basic pipeline structure."""
        errors = []
        
        # Check required top-level fields
        for field in self.validation_rules['required_fields']:
            if field not in pipeline_data:
                errors.append(f"Missing required field: {field}")
        
        # Check pipeline class
        if pipeline_data.get('class_id') != self.validation_rules['required_pipeline_class']:
            errors.append(f"Invalid pipeline class_id: {pipeline_data.get('class_id')}")
        
        return errors
    
    def _validate_uuids(self, pipeline_data: Dict[str, Any]) -> List[str]:
        """Validate UUID patterns and uniqueness."""
        errors = []
        seen_uuids = set()
        
        snap_map = pipeline_data.get('snap_map', {})
        
        for snap_id in snap_map.keys():
            # Check UUID format
            if not re.match(self.validation_rules['uuid_pattern'], snap_id):
                errors.append(f"Invalid UUID format: {snap_id}")
            
            # Check for duplicates
            if snap_id in seen_uuids:
                errors.append(f"Duplicate UUID: {snap_id}")
            seen_uuids.add(snap_id)
            
            # Check if using sequential pattern (recommended)
            if not re.match(self.validation_rules['sequential_uuid_pattern'], snap_id):
                # This is a warning, not an error
                pass
        
        return errors
    
    def _validate_snaps(self, pipeline_data: Dict[str, Any]) -> List[str]:
        """Validate snap configurations."""
        errors = []
        
        snap_map = pipeline_data.get('snap_map', {})
        
        for snap_id, snap_config in snap_map.items():
            # Check required snap fields
            required_snap_fields = ['class_id', 'instance_id', 'property_map']
            for field in required_snap_fields:
                if field not in snap_config:
                    errors.append(f"Snap {snap_id} missing required field: {field}")
            
            # Validate instance_id matches snap_id
            if snap_config.get('instance_id') != snap_id:
                errors.append(f"Snap {snap_id} instance_id mismatch")
            
            # Check for proper property_map structure
            prop_map = snap_config.get('property_map', {})
            if not isinstance(prop_map, dict):
                errors.append(f"Snap {snap_id} property_map must be a dictionary")
        
        return errors
    
    def _validate_links(self, pipeline_data: Dict[str, Any]) -> List[str]:
        """Validate link configurations."""
        errors = []
        
        snap_map = pipeline_data.get('snap_map', {})
        link_map = pipeline_data.get('link_map', {})
        
        for link_id, link_config in link_map.items():
            # Check required link fields
            required_link_fields = ['src_id', 'src_view_id', 'dst_id', 'dst_view_id']
            for field in required_link_fields:
                if field not in link_config:
                    errors.append(f"Link {link_id} missing required field: {field}")
            
            # Validate source and destination snaps exist
            src_id = link_config.get('src_id')
            dst_id = link_config.get('dst_id')
            
            if src_id and src_id not in snap_map:
                errors.append(f"Link {link_id} references non-existent source snap: {src_id}")
            
            if dst_id and dst_id not in snap_map:
                errors.append(f"Link {link_id} references non-existent destination snap: {dst_id}")
        
        return errors
    
    def _check_view_maps(self, pipeline_data: Dict[str, Any]) -> List[str]:
        """Check for potential view_map issues in multi-input snaps."""
        warnings = []
        
        snap_map = pipeline_data.get('snap_map', {})
        link_map = pipeline_data.get('link_map', {})
        
        # Count incoming links for each snap
        incoming_links = {}
        for link_config in link_map.values():
            dst_id = link_config.get('dst_id')
            if dst_id:
                incoming_links[dst_id] = incoming_links.get(dst_id, 0) + 1
        
        # Check snaps with multiple inputs
        for snap_id, snap_config in snap_map.items():
            if incoming_links.get(snap_id, 0) > 1:
                # This is a multi-input snap
                prop_map = snap_config.get('property_map', {})
                if 'view_map' not in prop_map:
                    warnings.append(
                        f"Multi-input snap {snap_id} ({snap_config.get('class_id', 'unknown')}) "
                        f"is missing view_map configuration. This may cause Designer display issues."
                    )
        
        return warnings
    
    def _get_detailed_info(self, pipeline_data: Dict[str, Any]) -> Dict[str, Any]:
        """Get detailed information about the pipeline."""
        snap_map = pipeline_data.get('snap_map', {})
        link_map = pipeline_data.get('link_map', {})
        
        snap_types = {}
        for snap_config in snap_map.values():
            class_id = snap_config.get('class_id', 'unknown')
            snap_types[class_id] = snap_types.get(class_id, 0) + 1
        
        return {
            'total_snaps': len(snap_map),
            'total_links': len(link_map),
            'snap_types': snap_types,
            'pipeline_class': pipeline_data.get('class_id'),
            'pipeline_version': pipeline_data.get('class_version')
        }
    
    def validate_expression_syntax(self, expression: str) -> Tuple[bool, str]:
        """
        Validate SnapLogic expression syntax.
        
        Args:
            expression: Expression string to validate
            
        Returns:
            Tuple of (is_valid, error_message)
        """
        # Basic expression validation rules
        
        # Check for unquoted string literals
        if '"' in expression and not expression.count('"') % 2 == 0:
            return False, "Unmatched quotes in expression"
        
        # Check for proper field references
        field_refs = re.findall(r'\\$\\w+', expression)
        param_refs = re.findall(r'_\\w+', expression)
        
        # More sophisticated validation could be added here
        return True, "Expression syntax appears valid"
    
    def get_validation_summary(self, pipeline_directory: str = ".") -> Dict[str, Any]:
        """
        Get validation summary for all pipelines in directory.
        
        Args:
            pipeline_directory: Directory to scan for .slp files
            
        Returns:
            Summary of validation results
        """
        pipeline_files = list(Path(pipeline_directory).glob("*.slp"))
        
        results = {
            'total_pipelines': len(pipeline_files),
            'valid_pipelines': 0,
            'invalid_pipelines': 0,
            'pipelines_with_warnings': 0,
            'details': []
        }
        
        for pipeline_file in pipeline_files:
            validation_result = self.validate(str(pipeline_file))
            
            if validation_result['valid']:
                results['valid_pipelines'] += 1
            else:
                results['invalid_pipelines'] += 1
            
            if validation_result['warnings']:
                results['pipelines_with_warnings'] += 1
            
            results['details'].append({
                'file': pipeline_file.name,
                'valid': validation_result['valid'],
                'error_count': len(validation_result['errors']),
                'warning_count': len(validation_result['warnings'])
            })
        
        return results