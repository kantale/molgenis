package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;

/**
 * Default implementation of the AttributeMetaData interface
 * 
 */
public class DefaultAttributeMetaData implements AttributeMetaData
{
	private final String name;
	private final FieldTypeEnum fieldType;
	private String description;
	private boolean nillable = true;
	private boolean readOnly = false;
	private Object defaultValue;
	private boolean idAttribute = false;
	private boolean labelAttribute = false;
	private boolean lookupAttribute = false;
	private EntityMetaData refEntity;
	private String label;
	private boolean visible = true;
	private boolean unique = false;
	private boolean auto = false;
	private Iterable<AttributeMetaData> attributesMetaData;
	private boolean aggregateable = false;
	private Range range;

	public DefaultAttributeMetaData(String name, FieldTypeEnum fieldType)
	{
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (fieldType == null) throw new IllegalArgumentException("FieldType cannot be null");
		this.name = name;
		this.fieldType = fieldType;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public FieldType getDataType()
	{
		return MolgenisFieldTypes.getType(fieldType.toString().toLowerCase());
	}

	@Override
	public boolean isNillable()
	{
		return nillable;
	}

	public void setNillable(boolean nillable)
	{
		this.nillable = nillable;
	}

	@Override
	public boolean isReadonly()
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	@Override
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean isIdAtrribute()
	{
		return idAttribute;
	}

	public void setIdAttribute(boolean idAttribute)
	{
		this.idAttribute = idAttribute;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return labelAttribute;
	}

	public void setLabelAttribute(boolean labelAttribute)
	{
		this.labelAttribute = labelAttribute;
	}

	@Override
	public EntityMetaData getRefEntity()
	{
		return refEntity;
	}

	public void setRefEntity(EntityMetaData refEntity)
	{
		this.refEntity = refEntity;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		return attributesMetaData;
	}

	public void setAttributesMetaData(Iterable<AttributeMetaData> attributesMetaData)
	{
		this.attributesMetaData = attributesMetaData;
	}

	@Override
	public String getLabel()
	{
		return label == null ? name : label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public boolean isUnique()
	{
		return unique;
	}

	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}

	@Override
	public boolean isAuto()
	{
		return auto;
	}

	public void setAuto(boolean auto)
	{
		this.auto = auto;
	}

	@Override
	public boolean isLookupAttribute()
	{
		return lookupAttribute;
	}

	public void setLookupAttribute(boolean lookupAttribute)
	{
		this.lookupAttribute = lookupAttribute;
	}

	@Override
	public boolean isAggregateable()
	{
		return this.aggregateable;
	}

	public void setAggregateable(boolean aggregateable)
	{
		this.aggregateable = aggregateable;
	}

	@Override
	public Range getRange()
	{
		return range;
	}

	public void setRange(Range range)
	{
		this.range = range;
	}

}
