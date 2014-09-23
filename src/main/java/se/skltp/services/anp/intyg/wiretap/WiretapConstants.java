/**
 * Copyright (c) 2014 Center for eHalsa i samverkan (CeHis).
 * 							<http://cehis.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.services.anp.intyg.wiretap;

public class WiretapConstants {

	/*
	 * Http header x-vp-sender-id, for Wiretap to use when acting consumer towards VP. 
	 * Http heaeder x-vp-instance-id, for Wiretap to use when acting consumer towards VP.
	 * 
	 * These two headers are dependent on each other in a way that when using x-vp-sender-id
	 * against VP, VP will check for a valid x-vp-instance-id.
	 */
	public static final String X_VP_SENDER_ID = "x-vp-sender-id";
	public static final String X_VP_INSTANCE_ID = "x-vp-instance-id";

	public static final String X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID = "x-rivta-original-serviceconsumer-hsaid";

}
