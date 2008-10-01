/***************************************************************************
 *   Filename: b64.h
 *   Author: ulno
 ***************************************************************************
 *   Copyright (C) 2008 by Ulrich Norbisrath 
 *   devel@mail.ulno.net   
 *                                                                         
 *   This program is free software; you can redistribute it and/or modify  
 *   it under the terms of the GNU Library General Public License as       
 *   published by the Free Software Foundation; either version 2 of the    
 *   License, or (at your option) any later version.                       
 *                                                                         
 *   This program is distributed in the hope that it will be useful,       
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         
 *   GNU General Public License for more details.                          
 *                                                                         
 *   You should have received a copy of the GNU Library General Public     
 *   License along with this program; if not, write to the                 
 *   Free Software Foundation, Inc.,                                       
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             
 ***************************************************************************
 *   Description:
 *   Interfacefile for the base64 encoding.
 ***************************************************************************/

#ifndef B64_H_
#define B64_H_

/* Here are ulno's extensions for coding whole strings,
 * they return the size of the output */
size_t b64encode( const char * src, char * dst, 
		size_t inputlen, size_t maxoutputlen );

size_t b64decode( const char * src, char * dst, 
		size_t inputlen, size_t maxoutputlen );


#endif /*B64_H_*/