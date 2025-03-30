<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\Desafio;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class desafioController extends Controller
{
    public function index()
    {
        $desafios = Desafio::all();
        return response()->json([ 'desafio' => $desafios, 'status' => 200 ], 200);
    }


    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'type' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'status' => 'required',
            'status_type' => 'required'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Error en la validación de datos',
                'errors' => $validator->errors(),
                'status' => 400
            ], 400);
        }

         // Subir la imagen si existe
        $imagePath = null;
            if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/desafios', 'public'); 
         }

         $desafio = Desafio::create([
            'name' => $request->name,
            'type' => $request->type,
            'image' => $imagePath, // Guarda la ruta en la base de datos
            'status' => $request->status,
            'status_type' => $request->status_type,
        ]);

        return response()->json(['desafio' => $desafio, 'status' => 201], 201);
    }


    public function show($id)
    {
        $desafio = Desafio::find($id);

        if (!$desafio) {
            return response()->json(['message' => 'Desafio no encontrado', 'status' => 404], 404);
        }

        return response()->json(['desafio' => $desafio, 'status' => 200], 200);
    }



    public function update(Request $request, $id)
    {
        $desafio = Desafio::find($id);

        if (!$desafio) {
            return response()->json(['message' => 'Desafio no encontrado', 'status' => 404], 404);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'type' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'status' => 'required',
            'status_type' => 'required'
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Error en la validación de datos', 'errors' => $validator->errors(), 'status' => 400], 400);
        }

        if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/desafios', 'public');
            $desafio->image = $imagePath;
        }

        $desafio->fill($request->except('image'));
        $desafio->save();

        return response()->json(['desafio' => $desafio, 'message' => 'Desafio actualizado', 'status' => 200], 200);
    }


    public function updatePartial(Request $request, $id)
    {
        $desafio = Desafio::find($id);

        if (!$desafio) {
            return response()->json(['message' => 'Desafio no encontrado', 'status' => 404], 404);
        }

        $desafio->fill($request->only([ 'name', 'type', 'image', 'status', 'status_type' ]));
        $desafio->save();

        return response()->json(['desafio' => $desafio, 'message' => 'Desafio actualizado parcialmente', 'status' => 200], 200);
    }


    public function destroy($id)
    {
        $desafio = Desafio::find($id);

        if (!$desafio) {
            return response()->json(['message' => 'Desafio no encontrado', 'status' => 404], 404);
        }

        $desafio->delete();

        return response()->json(['message' => 'Desafio eliminado', 'status' => 200], 200);
    }
}
