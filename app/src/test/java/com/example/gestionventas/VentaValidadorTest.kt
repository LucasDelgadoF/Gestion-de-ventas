package com.example.gestionventas

import com.example.gestionventas.model.VentaValidador
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VentaValidadorTest {

    @Test fun montoCeroEsInvalido() = assertNotNull(VentaValidador.validarMonto("0"))
    @Test fun montoNoNumericoEsInvalido() = assertNotNull(VentaValidador.validarMonto("abc"))
    @Test fun montoPositivoEsValido() = assertNull(VentaValidador.validarMonto("45000"))
    @Test fun codigoVacioEsInvalido() = assertNotNull(VentaValidador.validarCodigo(""))
    @Test fun codigoValido() = assertNull(VentaValidador.validarCodigo("AUT-1001"))
    @Test fun fechaInexistenteEsInvalida() = assertNotNull(VentaValidador.validarFecha("31/02/2026"))
    @Test fun fechaValida() = assertNull(VentaValidador.validarFecha("07/10/2026"))
    @Test fun tipoVacioEsInvalido() = assertNotNull(VentaValidador.validarTipo(""))
    @Test fun respaldoObligatorioAlEnviar() {
        val e = VentaValidador.validar("Convenio dental", "07/10/2026", "Cliente Ficticio", "1000", "AUT-1", null, true)
        assertNotNull(e.respaldo)
    }
}
