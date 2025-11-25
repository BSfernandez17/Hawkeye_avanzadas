
type Rol = 'ADMIN' | 'USER';

export interface Usuario {
    id: number;
    nombre: string;
    contrasena: string;
    email: string;
    rol: Rol;
    status: boolean;
    ip:string;
}