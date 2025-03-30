<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Usuario;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class usuarioController extends Controller
{
    public function index()
    {
        $usuarios = Usuario::all();
        $data = [
            'usuarios' => $usuarios,
            'status' => 200
        ];
        return response()->json ($usuarios, 200);
    }

    public function store(Request $request)
{
    $validator = Validator::make($request->all(), [
        'name' => 'required',
        'email' => 'required',
        'username' => 'required',
        'phone' => 'required',
        'password' => 'required',
    ]);
    
    if($validator->fails()){
        $data = [
            'message' => 'Error en la validación de datos',
            'errors' => $validator->errors(),
            'status' => 400
        ];
        return response()->json($data, 400);
    }
    
    $usuario = Usuario::create([
        'name' => $request->name,
        'email' => $request->email,
        'username' => $request->username,
        'phone' => $request->phone,
        'password' => $request->password,
    ]);
    
    if(!$usuario){
        $data = [
            'message' => 'Error al crear el usuario',
            'status' => 500
        ];
        return response()->json($data, 500);
    }
    
    $data = [
        'usuario' => $usuario,
        'status' => 201
    ];
    return response()->json($data, 201);

}
     
    

public function show($id)
    {
    $usuario = Usuario::find($id);
    
    if(!$usuario){
        $data = [
            'message' => 'Usuario no encontrado',
            'status' => 404
        ];
        return response()->json($data, 404);
    }
    
    $data = [
        'usuario' => $usuario,
        'status' => 200
    ];
    return response()->json($data, 200);
    }
    
    public function destroy($id)
    { 
        $usuario = Usuario::find($id);

        if(!$usuario){
            $data = [
                'message'=> 'usuario no encontrado',
                'status'=> 404

            ];
            return response()->json($data, 404);
        }
        $usuario->delete();
        $data = [
            'message'=> 'usuario eliminado',
            'status'=>200
        ];
        return response()->json($data, 200);

    }


   public function update(Request $request, $id)
   {
     $usuario = Usuario::find($id);

    if(!$usuario)
    {
        $data = [
          'message'=> 'usuario no encontrado',
           'status'=> 404
        ];  
        return response()->json($data, 404);
    }

       $validator = Validator::make($request->all(), [
         'name'=>'required',
         'email'=>'required',
         'username'=>'required',
         'phone'=>'required',
         'password'=> 'required'
    ]);

        if($validator->fails()){
          $data = [
            'message'=> 'Error en la validación de datos',
            'errors'=>$validator->errors(),
            'status'=> 400
        ];
           return response()->json($data, 400);
      }
      $usuario->name = $request->name;
      $usuario->email = $request->email;
      $usuario->username= $request->username;
      $usuario->phone = $request->phone;
      $usuario->password = $request->password;

      $usuario->save();

       $data = [
        'usuario' => $usuario,
        'message' => 'usuario actualizado',
        'status' => 200
       ];
       return response()->json($data, 200);

    }

    public function updatePartial(Request $request, $id)
{
    $usuario = Usuario::find($id);

    if(!$usuario)
    {
        $data = [
            'message'=> 'Usuario no encontrado',
            'status'=> 404
        ];  
        return response()->json($data, 404);
    }
    
   
    $validator = Validator::make($request->all(), [
        'name'=>'required',
        'email'=>'required',
        'username'=>'required',
        'phone'=>'required',
        'password'=> 'required' 
    ]);

    if($validator->fails()){
        $data = [
            'message'=> 'Error en la validación de datos',
            'errors'=>$validator->errors(),
            'status'=> 400
        ];
        return response()->json($data, 400); 
    }
    
    if($request->has('name')){
        $usuario->name = $request->name;
    }

    if($request->has('email')){
        $usuario->email = $request->email;
    }

    if($request->has('username')){
        $usuario->username = $request->username;
    }
    
    if($request->has('phone')){
        $usuario->phone = $request->phone;
    }
    
    if($request->has('password')){
        $usuario->password = $request->password;
    }

    $usuario->save();

    $data = [
        'usuario'=> $usuario,
        'message'=> 'Usuario actualizado',
        'status' => 200
    ];

    return response()->json($data, 200);
}

}