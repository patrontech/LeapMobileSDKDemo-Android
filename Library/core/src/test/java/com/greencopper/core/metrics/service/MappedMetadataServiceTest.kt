package com.greencopper.core.metrics.service

import com.greencopper.core.metrics.labels.EventParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MappedMetadataServiceTest {
    @Test
    fun whenMetadataStored_metadataIsStored(){
        //given
        val mappedMetadataService = MappedMetadataService()
        val eventParameter = EventParameter("parameter")
        val valueSent = "valueSent"

        //when
        mappedMetadataService[eventParameter] = valueSent

        //then
        assertThat(mappedMetadataService[eventParameter]).isEqualTo(valueSent)
    }
}