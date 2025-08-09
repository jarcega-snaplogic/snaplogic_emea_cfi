//the goal of this tjava row is to create pojo objects that correspond to an ordersin json request.
//Every line received is expected to one order and we can have one or more tour
globalMap.put("correlationId", java.util.UUID.randomUUID().toString());
if(input_row.orderInReference != null) globalMap.put("log_orderReferences", input_row.orderInReference);
output_row.correlationId = ((String)globalMap.get("correlationId"));

//passing tour reference along
output_row.shipment_reference = input_row.shipment_reference;
output_row.billOfLadingReference_MBL = input_row.billOfLadingReference_MBL;
output_row.container_reference = input_row.container_reference;
output_row.meta_messageFunction = input_row.meta_messageFunction;
output_row.VIN_Sequence = input_row.VIN_Sequence;
output_row.transportservicebuyer_ZZidentifier = input_row.transportservicebuyer_ZZidentifier;

//setting orderin variable type
OrderIn_RootOrderIn orderin;

//if the RootOrderIn object for this reference tour has been instanciated, recover it from global map
if (globalMap.containsKey(input_row.shipment_reference + input_row.billOfLadingReference_MBL + input_row.container_reference + input_row.meta_messageFunction + input_row.VIN_Sequence + input_row.transportservicebuyer_ZZidentifier))
{
	orderin = (OrderIn_RootOrderIn)globalMap.get(input_row.shipment_reference + input_row.billOfLadingReference_MBL + input_row.container_reference + input_row.meta_messageFunction + input_row.VIN_Sequence + input_row.transportservicebuyer_ZZidentifier);
}

//if it hasn't : create it with some mandatory fields and objects that should not repeat
else 
{
	orderin = new OrderIn_RootOrderIn();
	
	//populate meta
	orderin.getMeta().setSenderId(input_row.meta_senderId);
	orderin.getMeta().setDuplicateReceiverId("shippeo");    //mandatory default value
	orderin.getMeta().setMessageType("GTF511"); 
	if (input_row.meta_messageDate != null) {
		orderin.getMeta().setMessageDate(TalendDate.formatDateLocale("yyyy-MM-dd'T'HH:mm:ssZ",input_row.meta_messageDate,"fr_FR") );
	}
	else 
	{
		orderin.getMeta().setMessageDate(TalendDate.formatDateLocale("yyyy-MM-dd'T'HH:mm:ssZ",TalendDate.getCurrentDate(),"fr_FR") );
	}			//mandatory default value

	if (input_row.meta_messageReference!=null){
		orderin.getMeta().setMessageReference(input_row.meta_messageReference);
	}
	else
	{
		orderin.getMeta().setMessageReference(java.util.UUID.randomUUID().toString());
	}
	orderin.getMeta().setMessageFunction(input_row.meta_messageFunction);
	
	//populate shipment

	//root
	orderin.getShipment().setTechnicalReference(input_row.shipment_technicalReference); //mandatory
	if (!"1".equals(input_row.meta_messageFunction)) {
		orderin.getShipment().setTourReference(input_row.shipment_reference);
	}
	orderin.getShipment().setType(input_row.shipment_type); //mandatory
	globalMap.put(input_row.shipment_reference + input_row.billOfLadingReference_MBL + input_row.container_reference + input_row.meta_messageFunction + input_row.VIN_Sequence + input_row.transportservicebuyer_ZZidentifier, orderin); // mandatory

	//transportservicebuyer		
	//ZZ mandatory
	OrderIn_TransportServiceBuyer transportServiceBuyerZZ = new OrderIn_TransportServiceBuyer();
	transportServiceBuyerZZ.setQualifier("ZZ");
	if(input_row.transportservicebuyer_ZZidentifier == null) transportServiceBuyerZZ.setIdentifier("");
	else transportServiceBuyerZZ.setIdentifier(input_row.transportservicebuyer_ZZidentifier);
	orderin.getShipment().getTransportServiceBuyerList().add(transportServiceBuyerZZ);
	
	//PF optional
	if (!Relational.ISNULL(input_row.transportservicebuyer_PFidentifier)){
		OrderIn_TransportServiceBuyer transportServiceBuyerPF = new OrderIn_TransportServiceBuyer();
		transportServiceBuyerPF.setQualifier("PF");
		transportServiceBuyerPF.setIdentifier(input_row.transportservicebuyer_PFidentifier);
		orderin.getShipment().getTransportServiceBuyerList().add(transportServiceBuyerPF);
	};
	//VT optional
	if (!Relational.ISNULL(input_row.transportservicebuyer_VTidentifier)){
		OrderIn_TransportServiceBuyer transportServiceBuyerVT = new OrderIn_TransportServiceBuyer();
		transportServiceBuyerVT.setQualifier("VT");
		transportServiceBuyerVT.setIdentifier(input_row.transportservicebuyer_VTidentifier);
		orderin.getShipment().getTransportServiceBuyerList().add(transportServiceBuyerVT);
	};
	
	//LD optional
	if (input_row.transportservicebuyer_LDidentifier != null){
		OrderIn_TransportServiceBuyer transportServiceBuyerLD = new OrderIn_TransportServiceBuyer();
		transportServiceBuyerLD.setQualifier("LD");
		transportServiceBuyerLD.setIdentifier(input_row.transportservicebuyer_LDidentifier);
		orderin.getShipment().getTransportServiceBuyerList().add(transportServiceBuyerLD);

	};
	//carrier
	//ZZ or 00 mandatory but identifier can be empty string if needed on ZZ
	//ZZ or 00 mandatory but identifier can be empty string if needed on ZZ
	if(input_row.carrier_identifier00 == null || input_row.carrier_identifier00.equals("")){
	input_row.carrier_identifier00 = false ;
	}
	if(input_row.carrier_identifier00){
		OrderIn_Carrier carrier00 = new OrderIn_Carrier();
		carrier00.setQualifier("00");
		orderin.getShipment().getCarrierList().add(carrier00);
	}
	else {
		OrderIn_Carrier carrierZZ = new OrderIn_Carrier();
		carrierZZ.setQualifier("ZZ");
		if (input_row.carrier_identifierZZ == null){
			carrierZZ.setIdentifier(null);
		}
		else carrierZZ.setIdentifier(input_row.carrier_identifierZZ);
		orderin.getShipment().getCarrierList().add(carrierZZ);
	}
	//SA optional
	if(input_row.carrier_identifierSA != null){
		OrderIn_Carrier carrierSA = new OrderIn_Carrier();
		carrierSA.setQualifier("SA");
		carrierSA.setIdentifier(input_row.carrier_identifierSA);
		orderin.getShipment().getCarrierList().add(carrierSA);
	}

	//transportmean
	//List transportMeanList = new ArrayList<OrderIn_TransportMean>();
	//orderin.getShipment().setTransportMeanList(transportMeanList);
	
	/*if(input_row.transportMean_qualifier_1 != null && input_row.transportMean_identifier_1 != null) { //Temporary true, i need to find out exact conditons to fill a transportMean object
		OrderIn_TransportMean transportMean_1 = new OrderIn_TransportMean();
		transportMean_1.setQualifier(input_row.transportMean_qualifier_1); //mandatory
		transportMean_1.setText(input_row.transportMean_text_1);
		transportMean_1.setCode(input_row.transportMean_code_1);
		transportMean_1.setIdentificationPlate(input_row.transportMean_identificationPlate_1);
		transportMean_1.setIdentifier(input_row.transportMean_identifier_1); //mandatory
		transportMean_1.setIsTrackable(input_row.transportMean_isTrackable_1);
		transportMeanList.add(transportMean_1);
	};*/
	if( input_row.transportMean_list != null &&  !input_row.transportMean_list.isEmpty()){
		orderin.getShipment().setTransportMeanList(input_row.transportMean_list);
	}
	
	
	//transportmode
	orderin.getShipment().setTransportMode(input_row.shipment_transportMode);

	//isDangerousGoods
	orderin.getShipment().setisDangerousGoods(input_row.shipment_isDangerousGoods);	
	
	//nexttransportmode
	orderin.getShipment().setNextTransportMode(input_row.shipment_nextTransportMode);
	
	//tourtype
	orderin.getShipment().setTourType(input_row.shipment_tourType);
	
	//serviceLine
	orderin.getShipment().setServiceLine(input_row.shipment_serviceLine);
	//amounts
	if(input_row.amount_currency == "EUR"){
		List amountList = new ArrayList<OrderIn_Amount>();
		orderin.getShipment().setAmounts(amountList);
		OrderIn_Amount amount = new OrderIn_Amount();
		amount.setQualifier(input_row.amount_qualifier);
		amount.setValue(input_row.amount_value);
		amount.setCurrency(input_row.amount_currency);
		amountList.add(amount);
	}

	//complementaryServices
	if (input_row.complementaryService_qualifier != null){
		List complementaryServicesList = new ArrayList<OrderIn_ComplementaryService>();
		orderin.getShipment().setComplementaryServices(complementaryServicesList);
		OrderIn_ComplementaryService complementaryService = new OrderIn_ComplementaryService();
		complementaryService.setAmount(input_row.complementaryService_amount);
		complementaryService.setCurrency(input_row.complementaryService_currency);
		complementaryService.setIsChargeFree(input_row.complementaryService_isChargeFree);
		complementaryService.setQualifier(input_row.complementaryService_qualifier);
		complementaryService.setServiceCode(input_row.complementaryService_serviceCode);
		complementaryService.setTaxableUnitsNumber(input_row.complementaryService_taxableUnitsNumber);
		complementaryService.setText(input_row.complementaryService_text);
		complementaryServicesList.add(complementaryService);
	}
	//charges shipment
	if (input_row.charge_paiement != null){
		OrderIn_Charges charge = new OrderIn_Charges();
		orderin.getShipment().setCharges(charge);
		charge.setAnalyticalAccount(input_row.charge_analyticalAccount);
		charge.setBillingAccount(input_row.charge_billingAccount);
		charge.setCluster(input_row.charge_cluster);
		charge.setExemption(input_row.charge_exemption);
		charge.setIncotermCode(input_row.charge_incotermCode);
		charge.setIncotermLocation(input_row.charge_incotermLocation);
		charge.setPaiement(input_row.charge_paiement);
		charge.setServiceCharge(input_row.charge_serviceCharge);
		charge.setPreInvoiceLineItem(input_row.charge_preInvoiceLineItem);
		charge.setTaxableUnitsNumber(input_row.charge_taxableUnitsNumber);
		charge.setTaxableUnitsType(input_row.charge_taxableUnitsType);
	}

	//container
	if (input_row.container_reference != null){
		OrderIn_Container container = new OrderIn_Container();
		container.setReference(input_row.container_reference);
		orderin.getShipment().setContainer(container);
	}
	//billOfLadingReferences
	if(input_row.billOfLadingReference_MBL != null || input_row.billOfLadingReference_HBL != null){	
		List billOfLadingList = new ArrayList<OrderIn_BillOfLadingReference>();
		orderin.getShipment().setBillOfLadingReferences(billOfLadingList);
		
		//MBL
		if (input_row.billOfLadingReference_MBL != null){
			OrderIn_BillOfLadingReference billoflading_MBL = new OrderIn_BillOfLadingReference();
			billoflading_MBL.setQualifier("MBL");
			billoflading_MBL.setReference(input_row.billOfLadingReference_MBL);
			billOfLadingList.add(billoflading_MBL);
		};
		//HBL
		if (input_row.billOfLadingReference_HBL != null){
			OrderIn_BillOfLadingReference billoflading_HBL = new OrderIn_BillOfLadingReference();
			billoflading_HBL.setQualifier("HBL");
			billoflading_HBL.setReference(input_row.billOfLadingReference_HBL);
			billOfLadingList.add(billoflading_HBL);
		};
	};
	
	//status
	if(input_row.status_qualifier != null){
		OrderIn_Status status = new OrderIn_Status();
		status.setQualifier(input_row.status_qualifier);
		status.setEvent(input_row.status_event);
		orderin.getShipment().setStatus(status);
	};
}


//order level and below
OrderIn_Order order = orderin.getShipment().getOrder(input_row.order_reference_DQ);

/*Reference reference_test = new Reference();

reference_test.setQualifier("DQ");
reference_test.setReference(input_row.order_reference_DQ);

order.getReferences().add(reference_test);*/

if (order == null){
	order = new OrderIn_Order();
	//création order
	
	//reference
	//DQ
	if (input_row.order_reference_DQ != null){
		OrderIn_Reference referenceDQ = new OrderIn_Reference();
		referenceDQ.setQualifier("DQ");
		referenceDQ.setReference(input_row.order_reference_DQ);
		order.getReferences().add(referenceDQ);
	}
	//ADL
	if (input_row.order_reference_ADL != null && !"".equals(input_row.order_reference_ADL)){
		OrderIn_Reference referenceADL = new OrderIn_Reference();
		referenceADL.setQualifier("ADL");
		referenceADL.setReference(input_row.order_reference_ADL);
		order.getReferences().add(referenceADL);
	}
	//UCN
	if (input_row.order_reference_UCN != null && !"".equals(input_row.order_reference_UCN)){
		OrderIn_Reference referenceUCN = new OrderIn_Reference();
		referenceUCN.setQualifier("UCN");
		referenceUCN.setReference(input_row.order_reference_UCN);
		order.getReferences().add(referenceUCN);
	}
	//MA
	if (input_row.order_reference_MA != null && !"".equals(input_row.order_reference_MA)){
		OrderIn_Reference referenceMA = new OrderIn_Reference();
		referenceMA.setQualifier("MA");
		referenceMA.setReference(input_row.order_reference_MA);
		order.getReferences().add(referenceMA);
	}
	//AAO
	if (input_row.order_reference_AAO != null && !"".equals(input_row.order_reference_AAO)){
		OrderIn_Reference referenceAAO = new OrderIn_Reference();
		referenceAAO.setQualifier("AAO");
		referenceAAO.setReference(input_row.order_reference_AAO);
		order.getReferences().add(referenceAAO);
	}
	//ACN
	if (input_row.order_reference_ACN != null && !"".equals(input_row.order_reference_ACN)){
		OrderIn_Reference referenceACN = new OrderIn_Reference();
		referenceACN.setQualifier("ACN");
		referenceACN.setReference(input_row.order_reference_ACN);
		order.getReferences().add(referenceACN);
	}

	//pickup
	OrderIn_Pickup pickup = new OrderIn_Pickup();
	order.setPickup(pickup);
	
	//pickupidentification
	if(!Relational.ISNULL(input_row.pickup_identifier)){
		List pickupIdentificationList = new ArrayList<OrderIn_Identification>();
		OrderIn_Identification pickupIdentification = new OrderIn_Identification();
		pickupIdentification.setIdentifier(input_row.pickup_identifier);
			
		if(input_row.pickup_qualifier != null && !"".equals(input_row.pickup_qualifier)){	
			pickupIdentification.setQualifier(input_row.pickup_qualifier);
		}
		else{
			pickupIdentification.setQualifier("ZZ");
		}
		pickupIdentificationList.add(pickupIdentification);
		pickup.setIdentifications(pickupIdentificationList);
	}
	
	//pickup adress
	if(input_row.pickup_name != null)
	{
		if (StringHandling.LEN(input_row.pickup_name) > 35)
		{
			pickup.setName(input_row.pickup_name.substring(0,35));
		}
		else 
		{
			pickup.setName(input_row.pickup_name);
		}
	}
	
		if(input_row.pickup_address1 != null)
		{	
			if (input_row.pickup_address1.length() <= 35)
			{
				pickup.setAddress1(input_row.pickup_address1);
			}
			if ((input_row.pickup_address1.length()> 35) && (Relational.ISNULL(input_row.pickup_address2)))
			{
				pickup.setAddress1(input_row.pickup_address1.substring(0,35)) ;
				pickup.setAddress2(input_row.pickup_address1.substring(35, Math.min(70,input_row.pickup_address1.length())));		
			}
			if (input_row.pickup_address2 != null && input_row.pickup_address2 != "")
			{
				pickup.setAddress2(input_row.pickup_address2);
			}
					
		}
    pickup.setCountry(input_row.pickup_country);
	pickup.setPostalCode(input_row.pickup_postalCode);
	pickup.setCity(input_row.pickup_city);
	pickup.setLatitude(input_row.pickup_latitude);
	pickup.setLongitude(input_row.pickup_longitude);
	pickup.setInstructions(input_row.pickup_instructions);
	pickup.setAppointment(input_row.pickup_appointment);
	pickup.setOwnership(input_row.pickup_ownership);
	pickup.setActivityTime(input_row.pickup_activityTime);
	pickup.setState(input_row.pickup_state);
	//pickupdates
	List pickupDatesList = new ArrayList<OrderIn_ShippeoDate>();
	pickup.setDates(pickupDatesList);
	//pickupdate398
	if (input_row.pickup_date398 != null){
		OrderIn_ShippeoDate date398 = new OrderIn_ShippeoDate();
		date398.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'", input_row.pickup_date398));
		date398.setQualifier("398");
		pickupDatesList.add(date398);
	}
	//pickupdate474
	if (input_row.pickup_date474 != null){
		OrderIn_ShippeoDate date474 = new OrderIn_ShippeoDate();
		date474.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.pickup_date474));
		date474.setQualifier("474");
		pickupDatesList.add(date474);
	}
	//pickupdate473
	if (input_row.pickup_date473 != null){
		OrderIn_ShippeoDate date473 = new OrderIn_ShippeoDate();
		date473.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.pickup_date473));
		date473.setQualifier("473");
		pickupDatesList.add(date473);
	}
	//pickupdate510
	if (input_row.pickup_date510 != null){
		OrderIn_ShippeoDate date510 = new OrderIn_ShippeoDate();
		date510.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.pickup_date510));
		date510.setQualifier("510");
		pickupDatesList.add(date510);
	}
	//pickupdate234
	if (input_row.pickup_date234 != null){
		OrderIn_ShippeoDate date234 = new OrderIn_ShippeoDate();
		date234.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.pickup_date234));
		date234.setQualifier("234");
		pickupDatesList.add(date234);
	}
	//pickupdate235
	if (input_row.pickup_date235 != null){
		OrderIn_ShippeoDate date235 = new OrderIn_ShippeoDate();
		date235.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.pickup_date235));
		date235.setQualifier("235");
		pickupDatesList.add(date235);
	}
	
	
	

	//consignee
	OrderIn_Consignee consignee = new OrderIn_Consignee();
	order.setConsignee(consignee);
	
	//consigneeidentification
	if(!Relational.ISNULL(input_row.consignee_qualifier)){
		List consigneeIdentificationList = new ArrayList<OrderIn_Identification>();
		OrderIn_Identification consigneeIdentification = new OrderIn_Identification();
		consigneeIdentification.setIdentifier(input_row.consignee_identifier);	
		if(input_row.consignee_qualifier != null && !"".equals(input_row.consignee_qualifier)){	
			consigneeIdentification.setQualifier(input_row.consignee_qualifier);
		}
		else{
			consigneeIdentification.setQualifier("ZZ");
		}	
		
		consigneeIdentificationList.add(consigneeIdentification);
		consignee.setIdentifications(consigneeIdentificationList);
	}
	
	//consignee adress
	if(input_row.consignee_name != null)
	{
		if (StringHandling.LEN(input_row.consignee_name) > 35)
		{
			consignee.setName(input_row.consignee_name.substring(0,35));
		}
		else 
		{
			consignee.setName(input_row.consignee_name);
		}
	}
	
		if(input_row.consignee_address1 != null)
		{	
			if (input_row.consignee_address1.length() <= 35)
			{
				consignee.setAddress1(input_row.consignee_address1);
			}
		if ((input_row.consignee_address1.length()> 35) && (Relational.ISNULL(input_row.consignee_address2)))
			{
				consignee.setAddress1(input_row.consignee_address1.substring(0,35)) ;
				consignee.setAddress2(input_row.consignee_address1.substring(35,Math.min(70,input_row.consignee_address1.length())));		
			}
		if (input_row.consignee_address2 != null && input_row.consignee_address2 != "")
			{
				consignee.setAddress2(input_row.consignee_address2);
			}
					
		}
		
	consignee.setCountry(input_row.consignee_country);
	consignee.setPostalCode(input_row.consignee_postalCode);
	consignee.setCity(input_row.consignee_city);
	consignee.setLatitude(input_row.consignee_latitude);
	consignee.setLongitude(input_row.consignee_longitude);
	consignee.setInstructions(input_row.consignee_instructions);
	consignee.setAppointment(input_row.consignee_appointment);
	consignee.setOwnership(input_row.consignee_ownership);
	consignee.setActivityTime(input_row.consignee_activityTime);
	consignee.setState(input_row.consignee_state);
	//consigneedates
	List consigneeDatesList = new ArrayList<OrderIn_ShippeoDate>();
	consignee.setDates(consigneeDatesList);
	//consigneedate17
	if (input_row.consignee_date17 != null){
		OrderIn_ShippeoDate date17 = new OrderIn_ShippeoDate();
		date17.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.consignee_date17));
		date17.setQualifier("17");
		consigneeDatesList.add(date17);
	}
	//consigneedate474
	if (input_row.consignee_date474 != null){
		OrderIn_ShippeoDate consigneedate474 = new OrderIn_ShippeoDate();
		consigneedate474.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.consignee_date474));
		consigneedate474.setQualifier("474");
		consigneeDatesList.add(consigneedate474);
	}
	//consigneedate473
	if (input_row.consignee_date473 != null){
		OrderIn_ShippeoDate consigneedate473 = new OrderIn_ShippeoDate();
		consigneedate473.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.consignee_date473));
		consigneedate473.setQualifier("473");
		consigneeDatesList.add(consigneedate473);
	}
	//consigneedate64
	if (input_row.consignee_date64 != null){
		OrderIn_ShippeoDate date64 = new OrderIn_ShippeoDate();
		date64.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.consignee_date64));
		date64.setQualifier("64");
		consigneeDatesList.add(date64);
	}
	//consigneedate63
	if (input_row.consignee_date63 != null){
		OrderIn_ShippeoDate date63 = new OrderIn_ShippeoDate();
		date63.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.consignee_date63));
		date63.setQualifier("63");
		consigneeDatesList.add(date63);
	}
	//consigneedate2
	if (input_row.consignee_date2 != null){
		OrderIn_ShippeoDate date2 = new OrderIn_ShippeoDate();
		date2.setDateTime(TalendDate.formatDateInUTC("yyyy-MM-dd'T'HH:mm:ss'Z'",input_row.consignee_date2));
		date2.setQualifier("2");
		consigneeDatesList.add(date2);
	}
	//quantity
	if (input_row.quantity_fullLoad != null || input_row.quantity_grossVolume != null || input_row.quantity_grossWeight != null || input_row.quantity_loadingMeters != null || input_row.quantity_palletGround != null){
		OrderIn_Quantity quantity = new OrderIn_Quantity();
		quantity.setFullLoad(input_row.quantity_fullLoad);
		quantity.setGrossVolume(input_row.quantity_grossVolume);
		quantity.setGrossWeight(input_row.quantity_grossWeight);
		quantity.setLoadingMeters(input_row.quantity_loadingMeters);
		quantity.setPalletGround(input_row.quantity_palletGround);
		order.setQuantity(quantity);
	}
	//packing------------------------------------------------------------------------
	if( input_row.packing_list != null &&  !input_row.packing_list.isEmpty()){
		order.setPacking(input_row.packing_list);
	}
	//returnablepackaging
	if(input_row.returnablePacking_type != null && input_row.returnablePacking_number != null){
		List returnablePackagingList = new ArrayList<OrderIn_ReturnablePackaging>();
		OrderIn_ReturnablePackaging returnablePackaging = new OrderIn_ReturnablePackaging();
		returnablePackaging.setNumber(input_row.returnablePacking_number);
		returnablePackaging.setType(input_row.returnablePacking_type);
		returnablePackaging.setCustomType(input_row.returnablePacking_customType);
		returnablePackaging.setProductReference(input_row.returnablePacking_productReference);
		returnablePackaging.setProductDescription(input_row.returnablePacking_productDescription);
		returnablePackaging.setProductItem(input_row.returnablePacking_productItem);
		returnablePackagingList.add(returnablePackaging);
		order.setReturnablePackaging(returnablePackagingList);
		}
	//tags
	/*if(input_row.tag_label != null && input_row.tag_active != null){
		List tagList = new ArrayList<OrderIn_Tag>();
		OrderIn_Tag tag = new OrderIn_Tag();
		tag.setLabel(input_row.tag_label);
		tag.setActive(input_row.tag_active);
		tagList.add(tag);
		order.setTags(tagList);
	}*/
	if( input_row.tag_list != null &&  !input_row.tag_list.isEmpty()){
		order.setTags(input_row.tag_list);
	}
	//attributes
	/*if(input_row.attribute_name != null && input_row.attribute_value != null){
		List attributeList = new ArrayList<OrderIn_Attribute>();
		OrderIn_Attribute attribute = new OrderIn_Attribute();
		attribute.setName(input_row.attribute_name);
		attribute.setValue(input_row.attribute_value);
		attributeList.add(attribute);
		order.setAttributes(attributeList);
	}*/
	if( input_row.attribute_list != null &&  !input_row.attribute_list.isEmpty()){
		order.setAttributes(input_row.attribute_list);
	}
	//clientIdentification
	if(input_row.clientIdentification_organization != null || input_row.clientIdentification_agency != null){
		OrderIn_ClientIdentification clientIdentification= new OrderIn_ClientIdentification();
		clientIdentification.setOrganization(input_row.clientIdentification_organization);
		clientIdentification.setAgency(input_row.clientIdentification_agency);
		order.setClientIdentification(clientIdentification);
	}
	//amounts
	if(input_row.order_amount_qualifier != null && input_row.order_amount_value != null && input_row.order_amount_currency == "EUR"){
		List orderAmountList = new ArrayList<OrderIn_Amount>();
		OrderIn_Amount orderAmount = new OrderIn_Amount();
		orderAmount.setQualifier(input_row.order_amount_qualifier);
		orderAmount.setValue(input_row.order_amount_value);
		orderAmount.setCurrency(input_row.order_amount_currency);
		orderAmountList.add(orderAmount);
		order.setAmounts(orderAmountList);
	}
	//notificationcontacts
	if(input_row.notificationContact_name != null && (input_row.notificationContact_value != null || input_row.notificationContact_email != null)) {
		List notificationContactList = new ArrayList<OrderIn_NotificationContact>();
		OrderIn_NotificationContact notificationContact = new OrderIn_NotificationContact();
		notificationContactList.add(notificationContact);
		order.setNotificationContacts(notificationContactList);
		notificationContact.setName(input_row.notificationContact_name);
		notificationContact.setEmail(input_row.notificationContact_email);
		if(input_row.notificationContact_value != null){
		//communicationNumber
			OrderIn_CommunicationNumber communicationNumber = new OrderIn_CommunicationNumber();
			communicationNumber.setQualifier("AL");
			communicationNumber.setCountryCode(input_row.notificationContact_countryCode);
			communicationNumber.setValue(input_row.notificationContact_value);
			notificationContact.setCommunicationNumber(communicationNumber);
		}
		
	}
	
	//dangerousGoods
	if(input_row.dangerousGoods_ADR != null){
		List dangerousGoodList = new ArrayList<OrderIn_DangerousGood>();
		OrderIn_DangerousGood dangerousGood = new OrderIn_DangerousGood();
		order.setDangerousGoods(dangerousGoodList);
	
		//one fields
		dangerousGood.setAdr(input_row.dangerousGoods_ADR);
		dangerousGood.setClass_(input_row.dangerousGoods_class);
		dangerousGood.setClassificationCode(input_row.dangerousGoods_classificationCode);
		dangerousGood.setUndg(input_row.dangerousGoods_UNDG);
		dangerousGood.setPackingGroup(input_row.dangerousGoods_packingGroup);
		dangerousGood.setPackingInstruction(input_row.dangerousGoods_packingInstruction);
		dangerousGood.setCargoTransportAuthorisation(input_row.dangerousGoods_cargoTransportAuthorisation);
		dangerousGood.setTunnelRestrictionCode(input_row.dangerousGoods_tunnelRestrictionCode);
		dangerousGood.setGrossWeight(input_row.dangerousGoods_grossWeight);
		dangerousGood.setLimitedQuantity(input_row.dangerousGoods_limitedQuantity);
		dangerousGood.setExceptedQuantity(input_row.dangerousGoods_exceptedQuantity);
	}
	
	//charges order
	if(input_row.order_charge_paiement != null) {
		OrderIn_Charges chargeOrder = new OrderIn_Charges();
		chargeOrder.setPaiement(input_row.order_charge_paiement);
		chargeOrder.setExemption(input_row.order_charge_exemption);
		chargeOrder.setIncotermCode(input_row.order_charge_incotermCode);
		chargeOrder.setIncotermLocation(input_row.order_charge_incotermLocation);
		chargeOrder.setTaxableUnitsType(input_row.order_charge_taxableUnitsType);
		chargeOrder.setTaxableUnitsNumber(input_row.order_charge_taxableUnitsNumber);
		chargeOrder.setBillingAccount(input_row.order_charge_billingAccount);
		chargeOrder.setServiceCharge(input_row.order_charge_serviceCharge);
		chargeOrder.setAnalyticalAccount(input_row.order_charge_analyticalAccount);
		chargeOrder.setCluster(input_row.order_charge_cluster);
		chargeOrder.setPreInvoiceLineItem(input_row.order_charge_preInvoiceLineItem);
		order.setCharges(chargeOrder);
	}
	//goodsdescription
	
	order.setGoodsDescription(input_row.goodsDescription);
	
	// Shipment items
	if (input_row.item_list != null && !input_row.item_list.isEmpty()) {
		order.setItems(input_row.item_list);
	}
	
	orderin.getShipment().getOrders().add(order);
}

//handlingunits
//HandlingUnit handling // modifying the criteria of the If condition
if (input_row.handlingUnit_trackingCode != null || input_row.handlingUnit_packagingQualifier != null){
	//System.out.println(input_row.handlingUnit_contentReferences_item);
	List<OrderIn_HandlingUnit> handlingUnitList = order.getHandlingUnits();
	
	if (handlingUnitList == null){
		handlingUnitList = new ArrayList<OrderIn_HandlingUnit>();
		order.setHandlingUnits(handlingUnitList);
	}

	OrderIn_HandlingUnit handlingUnit = new OrderIn_HandlingUnit();
	handlingUnit.setTrackingCode(input_row.handlingUnit_trackingCode);
	handlingUnit.setBarcode(input_row.handlingUnit_barcode);
	handlingUnit.setPackagingQualifier(input_row.handlingUnit_packagingQualifier);
	handlingUnit.setCustomPackagingQualifier(input_row.handlingUnit_customPackagingQualifier);
	handlingUnit.setConsignorReference(input_row.handlingUnit_consignorReference);
	handlingUnit.setConsolidationId(input_row.handlingUnit_consolidationId);
	handlingUnit.setGrossWeight(input_row.handlingUnit_grossWeight);
	handlingUnit.setGrossVolume(input_row.handlingUnit_grossVolume);
	handlingUnit.setWidth(input_row.handlingUnit_width);
	handlingUnit.setLength(input_row.handlingUnit_length);
	handlingUnit.setHeight(input_row.handlingUnit_height);
	handlingUnit.setTrackingUrl(input_row.handlingUnit_trackingUrl);
	// putting the If condition on contentReferences to create the array only if there is values
	if (input_row.handlingUnit_contentReferences_item != null || input_row.handlingUnit_contentReferences_purchaseOrder != null){
		List<OrderIn_ContentReference> list_cr_hu = new ArrayList<OrderIn_ContentReference>();
		handlingUnit.setContentReferences(list_cr_hu);
		if(input_row.handlingUnit_contentReferences_item != null){
			OrderIn_ContentReference content_references_hu_item = new OrderIn_ContentReference();
			content_references_hu_item.setReference(input_row.handlingUnit_contentReferences_item);
			content_references_hu_item.setQualifier("item");
			list_cr_hu.add(content_references_hu_item);
		}
		if(input_row.handlingUnit_contentReferences_purchaseOrder != null){
			OrderIn_ContentReference content_references_hu_purchaseOrder = new OrderIn_ContentReference();
			content_references_hu_purchaseOrder.setReference(input_row.handlingUnit_contentReferences_purchaseOrder);
			content_references_hu_purchaseOrder.setQualifier("purchaseOrder");
			list_cr_hu.add(content_references_hu_purchaseOrder);
		}
	}

	handlingUnitList.add(handlingUnit);
}

// purchaseOrders
if (input_row.purchaseOrder_list != null && !input_row.purchaseOrder_list.isEmpty()) {
	order.setPurchaseOrders(input_row.purchaseOrder_list);
}
