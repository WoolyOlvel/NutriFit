<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\Plan_list;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class plan_listController extends Controller
{
    public function index()
    {
        $plan_list = Plan_list::all();
        return response()->json([ 'plan_list' => $plan_list, 'status' => 200 ], 200);
    }


    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'phone' => 'required',
            'tipo' => 'required',
            'tiempo' => 'required',
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
            $imagePath = $request->file('image')->store('images/plan_lists', 'public'); 
         }

         $plan_list = Plan_list::create([
            'name' => $request->name,
            'image' => $imagePath, // Guarda la ruta en la base de datos
            'phone' => $request->phone,
            'tipo' => $request->tipo,
            'tiempo' => $request->tiempo,
        ]);

        return response()->json(['plan_list' => $plan_list, 'status' => 201], 201);
    }


    public function show($id)
    {
        $plan_list = Plan_list::find($id);

        if (!$plan_list) {
            return response()->json(['message' => 'Lista no encontrada', 'status' => 404], 404);
        }

        return response()->json(['plan_list' => $plan_list, 'status' => 200], 200);
    }


    public function update(Request $request, $id)
    {
        $plan_list = Plan_list::find($id);

        if (!$plan_list) {
            return response()->json(['message' => 'Lista no encontrada', 'status' => 404], 404);
        }

        $validator = Validator::make($request->all(), [
           'name' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'phone' => 'required',
            'tipo' => 'required',
            'tiempo' => 'required',
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Error en la validación de datos', 'errors' => $validator->errors(), 'status' => 400], 400);
        }

        if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/appointments', 'public');
            $plan_list->image = $imagePath;
        }

        $plan_list->fill($request->except('image'));
        $plan_list->save();

        return response()->json(['patient' => $plan_list, 'message' => 'Lista actualizada', 'status' => 200], 200);
    }


    public function updatePartial(Request $request, $id)
    {
        $plan_list = Plan_list::find($id);

        if (!$plan_list) {
            return response()->json(['message' => 'Lista no encontrada', 'status' => 404], 404);
        }

        $plan_list->fill($request->only([ 'name', 'image', 'phone', 'tipo', 'tiempo']));
        $plan_list->save();

        return response()->json(['plan_list' => $plan_list, 'message' => 'Lista actualizada parcialmente', 'status' => 200], 200);
    }
}
