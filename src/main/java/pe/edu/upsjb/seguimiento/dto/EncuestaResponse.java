

package pe.edu.upsjb.seguimiento.dto;


public class EncuestaResponse {


    private int egresadoId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombresApellidos;
    private String genero;
    private String sede;
    private String facultad;
    private String carrera;
    private String anioEgreso;
    private String correoElectronico;
    private String numeroCelular;


    public int getEgresadoId() { return egresadoId; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombresApellidos() { return nombresApellidos; }
    public String getGenero() { return genero; }
    public String getSede() { return sede; }
    public String getFacultad() { return facultad; }
    public String getCarrera() { return carrera; }
    public String getAnioEgreso() { return anioEgreso; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getNumeroCelular() { return numeroCelular; }

    public void setEgresadoId(int egresadoId) { this.egresadoId = egresadoId; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public void setNombresApellidos(String nombresApellidos) { this.nombresApellidos = nombresApellidos; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setSede(String sede) { this.sede = sede; }
    public void setFacultad(String facultad) { this.facultad = facultad; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public void setAnioEgreso(String anioEgreso) { this.anioEgreso = anioEgreso; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public void setNumeroCelular(String numeroCelular) { this.numeroCelular = numeroCelular; }

}
